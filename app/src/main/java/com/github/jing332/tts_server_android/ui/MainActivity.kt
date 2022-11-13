package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.*
import com.github.jing332.tts_server_android.databinding.ActivityMainBinding
import com.github.jing332.tts_server_android.service.TtsIntentService
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.ui.systts.TtsSettingsActivity
import com.github.jing332.tts_server_android.util.MyTools
import com.github.jing332.tts_server_android.util.SharedPrefsUtils
import com.github.jing332.tts_server_android.util.toastOnUi
import tts_server_lib.Tts_server_lib


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val myReceiver: MyReceiver by lazy { MyReceiver() }
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(
            layoutInflater
        )
    }

    private val logList: ArrayList<MyLog> by lazy { ArrayList() }
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter(logList) }

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.rvLog.adapter = logAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        /*启动按钮*/
        binding.btnStart.setOnClickListener {
            SharedPrefsUtils.setPort(this, binding.etPort.text.toString().toInt())
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            startService(i)
        }
        /* 关闭按钮 */
        binding.btnClose.setOnClickListener {
            if (TtsIntentService.instance?.isRunning == true) { /*服务运行中*/
                TtsIntentService.instance?.closeServer() /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }

        /* 左上角抽屉按钮 */
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, 0, 0)
        toggle.syncState()
        binding.drawerLayout.addDrawerListener(toggle)
        /* 版本名 */
        val tv = binding.nav.getHeaderView(0).findViewById<TextView>(R.id.nav_header_subtitle)
        tv.text = BuildConfig.VERSION_NAME

        binding.nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_systts_settings ->
                    startActivity(Intent(this, TtsSettingsActivity::class.java))
                R.id.nav_settings ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_killBattery ->
                    killBattery()
                R.id.nav_checkUpdate ->
                    MyTools.checkUpdate(this)
                R.id.nav_about ->
                    showAboutDialog()

            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val port = SharedPrefsUtils.getPort(this)
        binding.etPort.setText(port.toString())
        if (TtsIntentService.instance?.isRunning == true) {
            setControlStatus(false)
            val localIp = Tts_server_lib.getOutboundIP()
            val msg = "服务已在运行, 监听地址: ${localIp}:${port}"
            logList.add(MyLog(LogLevel.WARN, msg))
        } else {
            val msg = "请点击启动按钮\n然后右上角菜单打开网页版↗️" +
                    "\n随后生成链接导入阅读APP即可使用" +
                    "\n\n关闭请点关闭按钮, 并等待响应。" +
                    "\n⚠️注意: 本APP需常驻后台运行！⚠️"
            logList.add(MyLog(LogLevel.INFO, msg))
        }

        /*注册广播*/
        IntentFilter(TtsIntentService.ACTION_ON_LOG).apply {
            addAction(TtsIntentService.ACTION_ON_STARTED)
            addAction(TtsIntentService.ACTION_ON_CLOSED)
            App.localBroadcast.registerReceiver(myReceiver, this)
        }

        MyTools.checkUpdate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(myReceiver)
    }

    /*右上角更多菜单*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /* 准备菜单 */
    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        /* 从配置文件读取并更新isWakeLock */
        val item = menu?.findItem(R.id.menu_wakeLock)
        item?.isChecked = SharedPrefsUtils.getWakeLock(this)

        return super.onCreateOptionsMenu(menu)
    }

    /*菜单点击事件*/
    @Suppress("DEPRECATION")
    @SuppressLint("BatteryLife")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_openWeb -> { /* {打开网页版} 按钮 */
                if (TtsIntentService.instance?.isRunning == true) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${TtsIntentService.instance?.cfg?.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    toastOnUi(R.string.please_start_service)
                }
            }
            R.id.menu_setToken -> {
                val token = SharedPrefsUtils.getToken(this)
                val builder = AlertDialog.Builder(this).setTitle(getString(R.string.set_token))
                val editText = EditText(this)
                editText.setText(token)
                builder.setView(editText)
                builder.setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    val text = editText.text.toString()
                    if (text != token) {
                        toastOnUi(getString(R.string.token_set_to) + text.ifEmpty { "空" })
                        SharedPrefsUtils.setToken(this, text)
                    }
                }.setNegativeButton(R.string.reset) { _, _ ->
                    SharedPrefsUtils.setToken(this, "")
                    toastOnUi(getString(R.string.ok_reset))
                }
                builder.create().show()
            }
            R.id.menu_wakeLock -> { /* 唤醒锁 */
                item.isChecked = !item.isChecked /* 更新选中状态 */
                SharedPrefsUtils.setWakeLock(this, item.isChecked)
                toastOnUi(R.string.restart_service_to_update)
            }
            R.id.menu_shortcut -> {
                MyTools.addShortcut(
                    this,
                    getString(R.string.app_switch),
                    "server_switch",
                    R.drawable.ic_switch,
                    Intent(this, ScSwitchActivity::class.java)
                )
            }
        }
        return true
    }

    /* 监听广播 */
    inner class MyReceiver : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                TtsIntentService.ACTION_ON_LOG -> {
                    val data = intent.getSerializableExtra("data") as MyLog
                    val layout = binding.rvLog.layoutManager as LinearLayoutManager
                    val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                    logAdapter.append(data)
                    /* 判断是否在最底部 */
                    if (isBottom)
                        binding.rvLog.scrollToPosition(logAdapter.itemCount - 1)
                }
                TtsIntentService.ACTION_ON_STARTED -> {
                    logAdapter.removeAll() /* 清空日志 */
                    setControlStatus(false)
                }
                TtsIntentService.ACTION_ON_CLOSED -> {
                    setControlStatus(true) /* 设置运行按钮可点击 */
                }
            }
        }
    }

    /* 设置底部按钮、端口 是否可点击 */
    fun setControlStatus(enable: Boolean) {
        if (enable) { //可点击{运行}按钮，编辑
            binding.etPort.isEnabled = true
            binding.btnStart.isEnabled = true
            binding.btnClose.isEnabled = false
        } else { //禁用按钮，编辑
            binding.etPort.isEnabled = false
            binding.btnStart.isEnabled = false
            binding.btnClose.isEnabled = true
        }
    }

    @Suppress("DEPRECATION")
    private fun showAboutDialog() {
        val dlg = AlertDialog.Builder(this)
        val tv = TextView(this)
        tv.movementMethod = LinkMovementMethod()
        val htmlStr =
            "Github开源地址: <a href = 'https://github.com/jing332/tts-server-android'>tts-server-android</a> <br/>" +
                    "特别感谢以下开源项目:  <br/>" +
                    "&emsp;<a href= 'https://github.com/asters1/tts'>asters1/tts(Go实现)</a>" +
                    "&emsp;<a href= 'https://github.com/wxxxcxx/ms-ra-forwarder'>ms-ra-forwarder</a>" +
                    "&emsp;<a href='https://github.com/ag2s20150909/TTS'>TTS APP</a>" +
                    "&emsp;<a href= 'https://github.com/gedoor/legado'>阅读APP</a>"
        tv.text = Html.fromHtml(htmlStr)
        tv.gravity = Gravity.CENTER /* 居中 */
        dlg.setView(tv)
        dlg.setTitle("关于")
            .setMessage("本应用界面使用Kotlin开发，底层服务由Go开发.")
            .create().show()
    }

    @SuppressLint("BatteryLife")
    private fun killBattery() {
        val intent = Intent()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                toastOnUi(R.string.added_background_whitelist)
            } else {
                try {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    toastOnUi(R.string.system_not_support_please_manual_set)
                    e.printStackTrace()
                }
            }
        }
    }
}
