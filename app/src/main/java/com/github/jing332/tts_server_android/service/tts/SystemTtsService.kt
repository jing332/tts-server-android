package com.github.jing332.tts_server_android.service.tts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioFormat
import android.os.Build
import android.os.PowerManager
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.service.tts.help.TtsManager
import com.github.jing332.tts_server_android.ui.TtsSettingsActivity
import com.github.jing332.tts_server_android.utils.GcManager
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
class SystemTtsService : TextToSpeechService() {
    companion object {
        const val TAG = "TtsService"
        const val ACTION_ON_CONFIG_CHANGED = "action_on_config_changed"
        const val ACTION_ON_LOG = "action_on_log"
        const val ACTION_CANCEL = "action_cancel"
        const val ACTION_KILL_PROCESS = "action_kill_process"
        const val NOTIFICATION_CHAN_ID = "system_tts_service"
        const val NOTIFICATION_ID = 2
    }

    private val mCurrentLanguage: MutableList<String> = mutableListOf("zho", "CHN", "")
    private val mTtsManager: TtsManager by lazy { TtsManager(this) }
    private val mReceiver: MyReceiver by lazy { MyReceiver() }
    private val mWakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return@lazy powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "tts-server:tts"
        )
    }


    override fun onCreate() {
        super.onCreate()
        /* 注册广播 */
        val intentFilter = IntentFilter(ACTION_ON_CONFIG_CHANGED)
        intentFilter.addAction(ACTION_KILL_PROCESS)
        intentFilter.addAction(ACTION_CANCEL)
        registerReceiver(mReceiver, intentFilter)

        mWakeLock.acquire(60 * 20 * 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        mWakeLock.release()
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return if (Locale.SIMPLIFIED_CHINESE.isO3Language == lang || Locale.US.isO3Language == lang) {
            if (Locale.SIMPLIFIED_CHINESE.isO3Country == country || Locale.US.isO3Country == country) TextToSpeech.LANG_COUNTRY_AVAILABLE else TextToSpeech.LANG_AVAILABLE
        } else TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onGetLanguage(): Array<String> {
        return mCurrentLanguage.toTypedArray()
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val result = onIsLanguageAvailable(lang, country, variant)
        mCurrentLanguage.clear()
        mCurrentLanguage.addAll(
            mutableListOf(
                lang.toString(),
                country.toString(),
                variant.toString()
            )
        )

        return result
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        mTtsManager.stop()
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        synchronized(this) {
            reNewWakeLock()
            startForegroundService()
            val text = request?.charSequenceText.toString().trim()
            updateNotification(getString(R.string.tts_state_playing), text)
            if (text.isBlank()) {
                callback?.start(
                    16000,
                    AudioFormat.ENCODING_PCM_16BIT, 1
                )
                callback?.done()
                return
            }
            runBlocking {
                mTtsManager.synthesizeText(request, callback)
            }
            updateNotification(
                getString(R.string.tts_state_idle),
                getString(R.string.auto_closed_later_notification)
            )
        }
    }

    private fun reNewWakeLock() {
        if (!mWakeLock.isHeld) {
            mWakeLock.acquire(60 * 20 * 1000)
            GcManager.doGC()
            Log.i(TAG, "刷新WakeLock 20分钟")
        }
    }


    private lateinit var mNotificationBuilder: Notification.Builder
    private lateinit var mNotificationManager: NotificationManager
    private var isNotificationDisplayed = false

    private val scope = object : CoroutineScope {
        override val coroutineContext = Job()
    }

    /* 启动前台服务通知 */
    private fun startForegroundService() {
        if (!isNotificationDisplayed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val chan = NotificationChannel(
                    NOTIFICATION_CHAN_ID,
                    getString(R.string.system_tts_service),
                    NotificationManager.IMPORTANCE_NONE
                )
                chan.lightColor = Color.CYAN
                chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                mNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.createNotificationChannel(chan)
            }
            startForeground(NOTIFICATION_ID, getNotification())
            isNotificationDisplayed = true

            scope.launch {
                while (true) {
                    delay(5000)
                    if (!mTtsManager.isSynthesizing) {
                        stopForeground(true)
                        isNotificationDisplayed = false
                        return@launch
                    }
                }
            }

        }
    }

    /* 更新通知 */
    private fun updateNotification(title: String, content: String) {
        val bigTextStyle = Notification.BigTextStyle().bigText(content).setSummaryText("TTS")
        mNotificationBuilder.style = bigTextStyle
        mNotificationBuilder.setContentTitle(title)
        mNotificationBuilder.setContentText(content)
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build())
    }

    /* 获取通知 */
    @Suppress("DEPRECATION")
    private fun getNotification(): Notification {
        val notification: Notification
        /*Android 12(S)+ 必须指定PendingIntent.FLAG_*/
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        /*点击通知跳转*/
        val pendingIntent =
            PendingIntent.getActivity(
                this, 0, Intent(
                    this,
                    TtsSettingsActivity::class.java
                ), pendingIntentFlags
            )

        val killProcessPendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(
                ACTION_KILL_PROCESS
            ), pendingIntentFlags
        )
        val cancelPendingIntent =
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_CANCEL), pendingIntentFlags)

        mNotificationBuilder = Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationBuilder.setChannelId(NOTIFICATION_CHAN_ID)
        }
        notification = mNotificationBuilder
            .setSmallIcon(R.mipmap.ic_app_notification)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .addAction(0, getString(R.string.kill_process), killProcessPendingIntent)
            .addAction(0, getString(R.string.cancel), cancelPendingIntent)
            .build()

        return notification
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_ON_CONFIG_CHANGED -> { /* 配置更改 */
                    mTtsManager.ttsConfig.loadConfig(this@SystemTtsService)
                }
                ACTION_KILL_PROCESS -> { /* 通知按钮{结束进程} */
                    stopForeground(true)
                    exitProcess(0)
                }
                ACTION_CANCEL -> { /* 通知按钮{取消}*/
                    if (mTtsManager.isSynthesizing)
                        onStop() /* 取消当前播放 */
                    else /* 无播放，关闭通知 */ {
                        stopForeground(true)
                        isNotificationDisplayed = false
                    }
                }
            }
        }
    }
}