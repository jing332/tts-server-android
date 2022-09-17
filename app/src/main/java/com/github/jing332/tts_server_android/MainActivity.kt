package com.github.jing332.tts_server_android

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.math.BigDecimal


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
            logList.add("请点击启动按钮, 然后在通知查看服务状态。")
            logList.add("关闭请点关闭按钮, 并等待响应。")
            logList.add("启动后可直接返回桌面，只要通知在就代表服务在运行中。")
        }

        adapter = LogViewAdapter(logList)
        rvLog.adapter = adapter
        val layoutManager = LinearLayoutManager(this@MainActivity)
        rvLog.layoutManager = layoutManager

        /*注册广播*/
        myReceiver = MyReceiver()
        var intentFilter = IntentFilter(TtsIntentService.ACTION_SEND)
        registerReceiver(myReceiver, intentFilter)
        /*启动按钮*/
        btnStart.setOnClickListener {
            adapter.removeAll()
            /*启动服务*/
            var i = Intent(this.applicationContext, TtsIntentService::class.java)
            i.putExtra("port", etPort.text.toString().toInt())
            startService(i)
            /*设置{启动}按钮为禁用*/
            setControlStatus(false)
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show()
        }
        /*关闭按钮*/
        btnClose.setOnClickListener {
            if (TtsIntentService.IsRunning) { /*服务运行中*/
                btnClose.isEnabled = false /*先禁用关闭按钮 以免多次点击*/
                TtsIntentService.closeServer(this) /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }

        checkUpdate()
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

    /*菜单点击事件*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> { /*{关于}按钮*/
                val dlg = AlertDialog.Builder(this)
                val tv = TextView(this)
                tv.movementMethod = LinkMovementMethod()

                val htmlStr = "特别感谢(他们的代码对我帮助很大):  <br />" +
                        "<a href='https://github.com/ag2s20150909/TTS'>TTS APP</a>" +
                        "&emsp;<a href= 'https://github.com/gedoor/legado'>阅读APP</a>"
                tv.text = Html.fromHtml(htmlStr)
                tv.gravity = Gravity.CENTER /*居中*/
                dlg.setView(tv)

                dlg.setTitle("关于")
                    .setMessage("本应用界面使用Kotlin开发，底层服务由Go开发.")
                    .create().show()
                true
            }
            R.id.menu_checkUpdate -> { /*{检查更新}按钮*/
                checkUpdate()
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
            val isQuit = intent?.getBooleanExtra("isQuit", false)
            runOnUiThread {
                when {
//                    isQuit == true -> { /*通知上的退出按钮，顺便关闭Activity。*/
//                        finish()
//                    }
                    isClosed == true -> { /*服务已关闭*/
                        setControlStatus(true) /*设置运行按钮可点击*/
                        Toast.makeText(ctx, "服务已关闭", Toast.LENGTH_SHORT).show()
                    }
                    logText?.isEmpty() == false -> { /*非空 追加日志*/
                        adapter.append(logText.toString())
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

    /*从Github检查更新*/
    private fun checkUpdate() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/jing332/tts-server-android/releases/latest")
            .get()
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d(TAG, "check update onFailure: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "检查更新失败 请检查网络", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    val s = response.body?.string()
                    val json = JSONObject(s)
                    val tag: String = json.getString("tag_name")
                    val downloadUrl: String =
                        json.getJSONArray("assets").getJSONObject(0)
                            .getString("browser_download_url")
                    val body: String = json.getString("body") /*本次更新内容*/

                    val versionName = BigDecimal(tag.split("_")[1].trim())
                    val pm = this@MainActivity.packageManager
                    val pi = pm.getPackageInfo(this@MainActivity.packageName, 0)
                    val appVersionName =
                        BigDecimal(pi.versionName.split("_").toTypedArray()[1].trim { it <= ' ' })
                    /*对比版本*/
                    if (appVersionName < versionName) {
                        downLoadAndInstall(body, downloadUrl, tag)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "不需要更新", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "检查更新失败", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun downLoadAndInstall(body: String, downloadUrl: String, tag: String) {
        try {
            runOnUiThread {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("有新版本")
                    .setMessage("版本号: $tag\n\n$body")
                    .setPositiveButton(
                        "Github下载"
                    ) { dialog: DialogInterface?, which: Int ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(downloadUrl)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .setNegativeButton(
                        "Github加速"
                    ) { dialog: DialogInterface?, which: Int ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://ghproxy.com/$downloadUrl")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .create().show()
            }
        } catch (ignored: java.lang.Exception) {
        }
    }
}
