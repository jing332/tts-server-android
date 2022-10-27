package com.github.jing332.tts_server_android.service

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.MainActivity
import tts_server_lib.LogCallback
import tts_server_lib.Tts_server_lib


@Suppress("DEPRECATION")
class TtsIntentService(name: String = "TtsIntentService") : IntentService(name) {
    companion object {
        const val TAG = "TtsIntentService"
        var ACTION_SEND = "service.send_log" /* 广播ID */
        const val ACTION_ON_LOG = "service.on_log"
        const val ACTION_ON_CLOSED = "service.on_closed"
        const val ACTION_ON_STARTED = "service.on_started"
        const val ACTION_NOTIFICATION_EXIT = "notification_exit"

        private var mIsWakeLock = false /* 是否使用唤醒锁 */
        var IsRunning = false /* 服务是否在运行 */
        var Isinited = false /* 已经初始化GoLib */
        var port: Int = 1233 /* 监听端口 */
        var token: String = ""

        /*关闭服务，如有Http请求需要等待*/
        fun closeServer(context: Context): Boolean {
            val err = Tts_server_lib.closeServer()/* 5s */
            if (err.isNotEmpty()) {
                Toast.makeText(context, "关闭失败：$err", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
    }

    private lateinit var mWakeLock: PowerManager.WakeLock /* 唤醒锁 */
    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    @Deprecated("Deprecated in Java")
    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        IsRunning = true
        port = intent?.getIntExtra("port", 1233) ?: 1233
        token = intent?.getStringExtra("token") ?: ""
        mIsWakeLock = intent?.getBooleanExtra("isWakeLock", false) ?: false

        initNotification()

        if (mIsWakeLock) { /* 启动唤醒锁 */
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            mWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "tts_server:ttsTag"
            )
            mWakeLock.acquire()
        }
        Toast.makeText(this, getString(R.string.service_started), Toast.LENGTH_SHORT).show()
        /* 注册广播 */
        registerReceiver(mReceiver, IntentFilter(ACTION_NOTIFICATION_EXIT))
        return super.onStartCommand(intent, flags, startId)
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        if (mIsWakeLock) { /* 释放唤醒锁 */
            mWakeLock.release()
        }
        Toast.makeText(this, getString(R.string.service_closed), Toast.LENGTH_SHORT).show()
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        if (!Isinited) { /* 初始化Go: 设置日志转发，注册Http.Server */
            /* 来自Go的日志 */
            val cb = LogCallback { level, msg ->
                Log.d(TAG, "$level $msg")
                sendLog(MyLog(level, msg))
            }
            Tts_server_lib.init(cb)
            Isinited = true
        }

        sendStartedMsg()
        /*启动Go服务并阻塞等待,直到关闭*/
        Tts_server_lib.runServer(port.toLong(), token)
        IsRunning = false
        sendClosedMsg()
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
                Intent(ACTION_NOTIFICATION_EXIT),
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
            .setContentText(getString(R.string.listen_address_local) + port)
            .setSmallIcon(smallIconRes)
            .setContentIntent(pendingIntent)
            .addAction(0, getString(R.string.exit), closePendingIntent)
            .build()
        startForeground(1, notification) //启动前台服务
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {/*点击通知上的退出按钮*/
            when (intent?.action) {
                ACTION_NOTIFICATION_EXIT -> {
                    closeServer(ctx!!)
                }
            }
        }
    }
}