@file:Suppress("OVERRIDE_DEPRECATION")

package com.github.jing332.tts_server_android.service.forwarder

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.ui.ImportConfigActivity
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.registerGlobalReceiver
import com.github.jing332.tts_server_android.utils.startForegroundCompat
import com.github.jing332.tts_server_android.utils.toast
import splitties.systemservices.powerManager
import tts_server_lib.Tts_server_lib

@Suppress("DEPRECATION")
abstract class AbsForwarderService(
    private val name: String,
    private val id: Int,
    private val actionLog: String,
    private val actionStarting: String,
    private val actionClosed: String,
    private val notificationChanId: String,
    @StringRes val notificationChanTitle: Int,
    @StringRes val notificationTitle: Int,
    @DrawableRes val notificationIcon: Int
) : IntentService(name) {
    private val notificationActionCopyUrl = "ACTION_NOTIFICATION_COPY_URL_$name"
    private val notificationActionClose = "ACTION_NOTIFICATION_CLOSE_$name"

    abstract fun initServer()
    abstract fun startServer()
    abstract fun closeServer()

    fun close() {
        if (isRunning)
            closeServer()
    }

    private var wakeLock: PowerManager.WakeLock? = null

    protected var isRunning: Boolean = false
    abstract val port: Int
    abstract val isWakeLockEnabled: Boolean

    private val mNotificationReceiver = NotificationActionReceiver()

    fun listenAddress(): String {
        return Tts_server_lib.getOutboundIP() + ":" + port
    }

    @SuppressLint("WakelockTimeout", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        isRunning = true

        initNotification()

        registerGlobalReceiver(
            listOf(notificationActionCopyUrl, notificationActionClose),
            mNotificationReceiver
        )

        if (isWakeLockEnabled) {
            wakeLock =
                powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "TTS_SERVER_ANDROID::$name"
                )
            wakeLock?.acquire()
        }
        initServer()
    }

    override fun onHandleIntent(intent: Intent?) {
        synchronized(this) {
            notifiStarted()
            kotlin.runCatching {
                startServer()
            }.onFailure {
                sendLog(LogLevel.ERROR, it.localizedMessage ?: it.toString())
            }
            notifiClosed()
        }
    }

    private fun notifiStarted() {
        val intent = Intent(actionStarting)
        AppConst.localBroadcast.sendBroadcast(intent)
    }

    private fun notifiClosed() {
        val intent = Intent(actionClosed)
        AppConst.localBroadcast.sendBroadcast(intent)
    }

    protected fun sendLog(log: AppLog) {
        val intent = Intent(actionLog).apply { putExtra(KeyConst.KEY_DATA, log) }
        AppConst.localBroadcast.sendBroadcast(intent)
    }

    protected fun sendLog(@LogLevel level: Int, msg: String) {
        sendLog(AppLog(level, msg))
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterReceiver(mNotificationReceiver)

        wakeLock?.release()
        wakeLock = null
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
                    ImportConfigActivity::class.java
                ).apply {
//                    putExtra(
//                        ImportConfigActivity.KEY_FRAGMENT_INDEX,
//                        ImportConfigActivity.INDEX_FORWARDER_SYS
//                    )
                },
                pendingIntentFlags
            )
        /*当点击退出按钮时发送广播*/
        val closePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(notificationActionClose),
                pendingIntentFlags
            )
        val copyAddressPendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(notificationActionCopyUrl),
                pendingIntentFlags
            )

        val smallIconRes: Int
        val builder = Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {/*Android 8.0+ 要求必须设置通知信道*/
            val chan = NotificationChannel(
                notificationChanId,
                getString(notificationChanTitle),
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            smallIconRes = notificationIcon
            builder.setChannelId(notificationChanId)
        } else {
            smallIconRes = R.mipmap.ic_app_notification
        }
        val notification = builder
            .setColor(ContextCompat.getColor(this, R.color.md_theme_light_primary))
            .setContentTitle(getString(notificationTitle))
            .setContentText(getString(R.string.server_listen_address_local, listenAddress()))
            .setSmallIcon(smallIconRes)
            .setContentIntent(pendingIntent)
            .addAction(0, getString(R.string.exit), closePendingIntent)
            .addAction(0, getString(R.string.copy_address), copyAddressPendingIntent)
            .build()

        // 前台服务
        startForegroundCompat(id, notification)
    }

    inner class NotificationActionReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                notificationActionCopyUrl -> {
                    ClipboardUtils.copyText(listenAddress())
                    toast(R.string.copied)
                }

                notificationActionClose -> {
                    closeServer()
                }

            }
        }
    }
}