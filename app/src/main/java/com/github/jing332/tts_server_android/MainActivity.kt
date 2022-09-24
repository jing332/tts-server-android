package com.github.jing332.tts_server_android

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    lateinit var etPort: EditText
    lateinit var btnStart: Button
    lateinit var btnClose: Button

    lateinit var rvLog: RecyclerView
    lateinit var logList: ArrayList<String>
    lateinit var adapter: LogViewAdapter

    lateinit var myReceiver: MyReceiver
    var mLastPosition = -1
    var mLastItemCount = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvLog = findViewById(R.id.rv_log)
        etPort = findViewById(R.id.et_port)
        btnStart = findViewById(R.id.btn_start)
        btnClose = findViewById(R.id.btn_close)

        logList = ArrayList()
        if (TtsIntentService.IsRunning) { //服务在运行
            etPort.setText(TtsIntentService.port.toString()) //设置监听端口
            setControlStatus(false)
            logList.add("服务已在运行, 监听地址: localhost:${TtsIntentService.port}")
        } else {
            logList.add("请点击启动按钮")
            logList.add("然后右上角菜单打开网页版↗️")
            logList.add("随后生成链接导入阅读APP即可使用\n")
            logList.add("关闭请点关闭按钮, 并等待响应。")
            logList.add("⚠️注意: 本APP需常驻后台运行！⚠️")
        }

        adapter = LogViewAdapter(logList)
        rvLog.adapter = adapter
        val layoutManager = LinearLayoutManager(this@MainActivity)
        layoutManager.stackFromEnd = true
        rvLog.layoutManager = layoutManager

        rvLog.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                mLastItemCount = recyclerView.layoutManager!!.itemCount
                /* 当前状态为停止滑动状态SCROLL_STATE_IDLE时 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.layoutManager is LinearLayoutManager) {
                        mLastPosition = layoutManager.findLastVisibleItemPosition()
                    }
                }
            }
        })


        /*注册广播*/
        myReceiver = MyReceiver()
        val intentFilter = IntentFilter(TtsIntentService.ACTION_SEND)
        registerReceiver(myReceiver, intentFilter)
        /*启动按钮*/
        btnStart.setOnClickListener {
            adapter.removeAll()
            /*启动服务*/
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            i.putExtra("port", etPort.text.toString().toInt())
            startService(i)
            /*设置{启动}按钮为禁用*/
            setControlStatus(false)
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show()
        }
        /*关闭按钮*/
        btnClose.setOnClickListener {
            if (TtsIntentService.IsRunning) { /*服务运行中*/
                TtsIntentService.closeServer(this) /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }

        MyTools.checkUpdate(this)
    }

    /*点击返回键返回桌面而不是退出程序*/
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val home = Intent(Intent.ACTION_MAIN)
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            home.addCategory(Intent.CATEGORY_HOME)
            startActivity(home)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /*右上角更多菜单*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflation: MenuInflater = menuInflater
        inflation.inflate(R.menu.menu_main, menu)
        return true
    }

    /* 准备菜单 */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.menu_wakeLock)
        item?.isCheckable = true /* 设置{唤醒锁}菜单为可选中的 */
        /* 从配置文件读取并更新isWakeLock */
        TtsIntentService.isWakeLock = SharedPrefsUtils.getWakeLock(this)
        item?.isChecked = TtsIntentService.isWakeLock
        return true
    }

    /*菜单点击事件*/
    @SuppressLint("BatteryLife")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> { /*{关于}按钮*/
                val dlg = AlertDialog.Builder(this)
                val tv = TextView(this)
                tv.movementMethod = LinkMovementMethod()

                val htmlStr = "特别感谢(他们的代码对我帮助很大):  <br />" +
                        "&emsp;<a href= 'https://github.com/asters1/tts'>asters1/tts(Go实现)</a>" +
                        "&emsp;<a href= 'https://github.com/wxxxcxx/ms-ra-forwarder'>ms-ra-forwarder</a>" +
                        "<a href='https://github.com/ag2s20150909/TTS'>TTS APP</a>" +
                        "&emsp;<a href= 'https://github.com/gedoor/legado'>阅读APP</a>"


                tv.text = Html.fromHtml(htmlStr)
                tv.gravity = Gravity.CENTER /* 居中 */
                dlg.setView(tv)

                dlg.setTitle("关于")
                    .setMessage("本应用界面使用Kotlin开发，底层服务由Go开发.")
                    .create().show()
                true
            }
            R.id.menu_checkUpdate -> { /* {检查更新}按钮 */
                MyTools.checkUpdate(this)
                true
            }
            R.id.menu_openWeb -> { /* {打开网页版} 按钮 */
                if (TtsIntentService.IsRunning) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("http://localhost:${TtsIntentService.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "请先启动服务！", Toast.LENGTH_LONG).show()
                }

                true
            }
            R.id.menu_killBattery -> { /* {电池优化}按钮 */
                val intent = Intent()
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (pm.isIgnoringBatteryOptimizations(packageName)) {
                        Toast.makeText(this, "已忽略电池优化", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "系统不支持 请手动操作", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
                true
            }
            R.id.menu_wakeLock -> { /* 唤醒锁 */
                item.isChecked = !item.isChecked /* 更新选中状态 */
                TtsIntentService.isWakeLock = item.isChecked
                SharedPrefsUtils.setWakeLock(this, item.isChecked)
                Toast.makeText(this, "${item.isChecked} 重启服务以生效", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //监听广播
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val logText = intent?.getStringExtra("sendLog")
            val isClosed = intent?.getBooleanExtra("isClosed", false)
            runOnUiThread {
                when {
                    isClosed == true -> { /*服务已关闭*/
                        setControlStatus(true) /*设置运行按钮可点击*/
                        Toast.makeText(ctx, "服务已关闭", Toast.LENGTH_SHORT).show()
                    }
                    logText?.isEmpty() == false -> { /*非空 追加日志*/
                        adapter.append(logText.toString())
                        /* 判断是否在最底部 */
                        if (mLastPosition == mLastItemCount - 1 || mLastPosition == mLastItemCount - 2) {
                            rvLog.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    /*设置底部按钮、端口 是否可点击*/
    fun setControlStatus(enable: Boolean) {
        if (enable) { //可点击{运行}按钮，编辑
            etPort.isEnabled = true
            btnStart.isEnabled = true
            btnClose.isEnabled = false
        } else { //禁用按钮，编辑
            etPort.isEnabled = false
            btnStart.isEnabled = false
            btnClose.isEnabled = true
        }
    }
}
