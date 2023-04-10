@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package com.github.jing332.tts_server_android.service.forwarder.system

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.SystemNotificationConst
import com.github.jing332.tts_server_android.help.LocalTtsEngineHelper
import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.model.speech.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.MainActivity.Companion.INDEX_FORWARDER_SYS
import com.github.jing332.tts_server_android.ui.MainActivity.Companion.KEY_FRAGMENT_INDEX
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import tts_server_lib.SysTtsForwarder
import tts_server_lib.Tts_server_lib

class SysTtsForwarderService : IntentService("ApiConvIntentService") {
    companion object {
        const val TAG = "SysTtsServerService"
        const val ACTION_REQUEST_CLOSE_SERVER = "ACTION_REQUEST_CLOSE_SERVER"
        const val ACTION_ON_CLOSED = "ACTION_ON_CLOSED"
        const val ACTION_ON_STARTING = "ACTION_ON_STARTING"
        const val ACTION_ON_LOG = "ACTION_ON_LOG"

        const val ACTION_NOTIFICATION_COPY_URL = "ACTION_NOTIFICATION_COPY_URL"
        const val ACTION_NOTIFICATION_EXIT = "ACTION_NOTIFICATION_STOP"

        fun requestCloseServer() {
            App.localBroadcast.sendBroadcast(Intent(ACTION_REQUEST_CLOSE_SERVER))
        }

        val isRunning: Boolean
            get() = instance?.isRunning == true

        var instance: SysTtsForwarderService? = null
    }

    init {
        instance = this
    }

    val listenAddress: String
        get() {
            val localIp = Tts_server_lib.getOutboundIP()
            return "${localIp}:${mCfg.port}"
        }

    var isRunning = false
        private set

    private var mServer: SysTtsForwarder? = null
    private var mLocalTTS: LocalTTS? = null
    private val mLocalTtsHelper by lazy { LocalTtsEngineHelper(this) }

    private var mWakeLock: PowerManager.WakeLock? = null

    private val mCfg by lazy { SysTtsForwarderConfig }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mCfg.isWakeLockEnabled) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            mWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "tts_server:systts-forwarder"
            )
        }

        initNotification()
        mServer = SysTtsForwarder().apply {
            initCallback(object : tts_server_lib.SysTtsForwarderCallback {
                override fun log(level: Int, msg: String) {
                    sendLog(level, msg)
                    mWakeLock?.acquire(30 * 60 * 1000L /*10 minutes*/)
                }

                override fun cancelAudio(engine: String) {
                    if (mLocalTTS?.engine == engine) {
                        mLocalTTS?.onStop()
                        sendLog(LogLevel.WARN, "Canceled: $engine")
                    }
                }

                override fun getAudio(engine: String, text: String, rate: Int): String {
                    if (mLocalTTS?.engine != engine) {
                        mLocalTTS?.onDestroy()
                        mLocalTTS = LocalTTS(engine)
                    }

                    mLocalTTS?.let {
                        val file = it.getAudioFile(text, rate)
                        if (file.exists()) return file.absolutePath
                    }
                    throw Exception(getString(R.string.forwarder_sys_fail_audio_file))
                }

                override fun getEngines(): String {
                    val data = getSysTtsEngines().map { EngineInfo(it.name, it.label) }
                    return App.jsonBuilder.encodeToString(data)
                }

                override fun getVoices(engine: String): String {
                    return runBlocking {
                        val ok = mLocalTtsHelper.setEngine(engine)
                        if (!ok) throw Exception(getString(R.string.systts_engine_init_failed_timeout))

                        val data = mLocalTtsHelper.voices.map {
                            VoiceInfo(
                                it.name,
                                it.locale.toLanguageTag(),
                                it.locale.getDisplayName(it.locale),
                                it.features?.toList()
                            )
                        }

                        return@runBlocking App.jsonBuilder.encodeToString(data)
                    }
                }
            })
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendLog(level: Int, msg: String) {
        Log.d(TAG, "$level, $msg")
        val intent = Intent(ACTION_ON_LOG).putExtra(KeyConst.KEY_DATA, AppLog(level, msg))
        App.localBroadcast.sendBroadcast(intent)
    }

    override fun onHandleIntent(intent: Intent?) {
        isRunning = true
        mServer?.let {
            toast(R.string.service_started)
            App.localBroadcast.sendBroadcast(Intent(ACTION_ON_STARTING))
            it.start(mCfg.port.toLong())
            toast(R.string.service_closed)
            App.localBroadcast.sendBroadcast(Intent(ACTION_ON_CLOSED))
        }
        isRunning = false
    }

    private val mReceiver by lazy { MyReceiver() }
    private val mNotificationReceiver by lazy { NotificationActionReceiver() }

    override fun onCreate() {
        super.onCreate()
        App.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_REQUEST_CLOSE_SERVER))
        registerReceiver(mNotificationReceiver, IntentFilter(ACTION_NOTIFICATION_COPY_URL).apply {
            addAction(ACTION_NOTIFICATION_EXIT)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
        unregisterReceiver(mNotificationReceiver)
        stopForeground(true)
        mWakeLock?.release()
    }

    private fun initNotification() {
        /*Android 12(S)+ 必须指定PendingIntent.FLAG_*/
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE
        else
            0

        /*点击通知跳转*/
        val pendingIntent =
            PendingIntent.getActivity(
                this, 0, Intent(
                    this,
                    MainActivity::class.java
                ).apply { putExtra(KEY_FRAGMENT_INDEX, INDEX_FORWARDER_SYS) },
                pendingIntentFlags
            )
        /*当点击退出按钮时发送广播*/
        val closePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_NOTIFICATION_EXIT),
                pendingIntentFlags
            )
        val copyAddressPendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_NOTIFICATION_COPY_URL),
                pendingIntentFlags
            )

        val chanId = "systts_forwarder_status"
        val smallIconRes: Int
        val builder = Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {/*Android 8.0+ 要求必须设置通知信道*/
            val chan = NotificationChannel(
                chanId,
                getString(R.string.forwarder_systts),
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            smallIconRes = R.drawable.ic_baseline_compare_arrows_24
            builder.setChannelId(chanId)
        } else {
            smallIconRes = R.mipmap.ic_app_notification
        }
        val notification = builder
            .setColor(ContextCompat.getColor(this, R.color.md_theme_light_primary))
            .setContentTitle(getString(R.string.forwarder_sys_running))
            .setContentText(getString(R.string.server_listen_address_local, listenAddress))
            .setSmallIcon(smallIconRes)
            .setContentIntent(pendingIntent)
            .addAction(0, getString(R.string.exit), closePendingIntent)
            .addAction(0, getString(R.string.copy_address), copyAddressPendingIntent)
            .build()
        startForeground(SystemNotificationConst.ID_FORWARDER_SYS, notification) //启动前台服务
    }

    inner class NotificationActionReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_NOTIFICATION_COPY_URL -> {
                    ClipboardUtils.copyText(listenAddress)
                    toast(R.string.copied)
                }

                ACTION_NOTIFICATION_EXIT -> {
                    App.localBroadcast.sendBroadcast(Intent(ACTION_REQUEST_CLOSE_SERVER))
                }

            }
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_REQUEST_CLOSE_SERVER -> kotlin.runCatching {
                    mServer?.close()
                }.onFailure {
                    longToast("Close failed：${it.message}")
                }
            }
        }
    }

    private fun getSysTtsEngines(): List<TextToSpeech.EngineInfo> {
        val tts = TextToSpeech(App.context, null)
        val engines = tts.engines
        tts.shutdown()
        return engines
    }

}