package com.github.jing332.tts_server_android.service

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.util.toastOnUi
import tts_server_lib.LogCallback
import tts_server_lib.Tts_server_lib


@Suppress("DEPRECATION")
class TtsIntentService(name: String = "TtsIntentService") : IntentService(name) {
    companion object {
        const val TAG = "TtsIntentService"
        const val ACTION_ON_LOG = "service.on_log"
        const val ACTION_ON_CLOSED = "service.on_closed"
        const val ACTION_ON_STARTED = "service.on_started"
        const val ACTION_NOTIFY_EXIT = "notify_exit" /* 通知的{退出}按钮*/
        const val ACTION_NOTIFY_COPY_ADDRESS = "notify_copy_address"
        var instance: TtsIntentService? = null

    }

    private var listenAddress = ""
    private val mReceiver: MyReceiver by lazy { MyReceiver() }
    private val mWakeLock: PowerManager.WakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "tts_server:ttsTag"
        )
    }

    var isRunning = false /* 服务是否在运行 */
        private set
    lateinit var cfg: TtsServerConfig
        private set

    @Deprecated("Deprecated in Java")
    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, getString(R.string.service_started), Toast.LENGTH_SHORT).show()
        instance = this
        isRunning = true
        cfg = TtsServerConfig(this)
        val localIp = Tts_server_lib.getOutboundIP()
        listenAddress = "${localIp}:${cfg.port}"

        initNotification()
        if (cfg.isWakeLock) mWakeLock.acquire()

        /* 注册广播 */
        IntentFilter(ACTION_NOTIFY_EXIT).apply {
            addAction(ACTION_NOTIFY_COPY_ADDRESS)
            registerReceiver(mReceiver, this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        if (cfg.isWakeLock) mWakeLock.release()
        unregisterReceiver(mReceiver)
        stopForeground(true)
        Toast.makeText(this, getString(R.string.service_closed), Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        sendStartedMsg()
        /* 初始化Go: 设置日志转发，注册Http.Server */
        val cb = LogCallback { level, msg ->
            Log.d(TAG, "$level $msg")
            sendLog(MyLog(level, msg))
        }
        Tts_server_lib.init(cb)
        /*启动Go服务并阻塞等待,直到关闭*/
        Tts_server_lib.runServer(cfg.port.toLong(), cfg.token, cfg.isUseDnsEdge)
        isRunning = false
        sendClosedMsg()
    }

    /* 强制关闭服务 */
    fun closeServer() {
        Tts_server_lib.closeServer()
    }

    /* 广播日志消息 */
    private fun sendLog(data: MyLog) {
        val i = Intent(ACTION_ON_LOG)
        i.putExtra("data", data)
        sendBroadcast(i)
    }

    /* 广播启动消息 */
    private fun sendStartedMsg() {
        val i = Intent(ACTION_ON_STARTED)
        sendBroadcast(i)
    }

    /* 广播关闭消息 */
    private fun sendClosedMsg() {
        val i = Intent(ACTION_ON_CLOSED)
        sendBroadcast(i)
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
                ), pendingIntentFlags
            )
        /*当点击退出按钮时发送广播*/
        val closePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_NOTIFY_EXIT),
                pendingIntentFlags
            )
        val copyAddrPendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_NOTIFY_COPY_ADDRESS),
                pendingIntentFlags
            )

        val chanId = "server_status"
        val smallIconRes: Int
        val builder = Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {/*Android 8.0+ 要求必须设置通知信道*/
            val chan = NotificationChannel(
                chanId,
                getString(R.string.tts_server_status),
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            smallIconRes = R.drawable.ic_app_notification
            builder.setChannelId(chanId)
        } else {
            smallIconRes = R.mipmap.ic_app_notification
        }
        val notification = builder
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(getString(R.string.tts_server_running))
            .setContentText(getString(R.string.listen_address_local, listenAddress))
            .setSmallIcon(smallIconRes)
            .setContentIntent(pendingIntent)
            .addAction(0, getString(R.string.exit), closePendingIntent)
            .addAction(0, getString(R.string.copy_address), copyAddrPendingIntent)
            .build()
        startForeground(1, notification) //启动前台服务
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {/*点击通知上的退出按钮*/
            when (intent?.action) {
                ACTION_NOTIFY_EXIT -> {
                    closeServer()
                }
                ACTION_NOTIFY_COPY_ADDRESS -> {
                    val cm = ctx?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    cm?.setPrimaryClip(ClipData.newPlainText("address", listenAddress))
                    ctx?.toastOnUi(R.string.copied)
                }
            }
        }
    }
}