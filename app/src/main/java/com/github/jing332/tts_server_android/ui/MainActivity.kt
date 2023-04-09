package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.core.view.setPadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ShortCuts
import com.github.jing332.tts_server_android.databinding.MainActivityBinding
import com.github.jing332.tts_server_android.databinding.MainDrawerNavHeaderBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.util.*
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val TAG = "MainActivity"
        const val ACTION_BACK_KEY_DOWN = "ACTION_BACK_KEY_DOWN"
        const val KEY_FRAGMENT_INDEX = "KEY_INDEX"

        const val INDEX_SYS_TTS = 0
        const val INDEX_FORWARDER_SYS = 1
        const val INDEX_FORWARDER_MS = 2

        private val drawerMenus by lazy {
            listOf(R.id.nav_systts, R.id.nav_systts_forwarder, R.id.nav_server, R.id.nav_settings)
        }
    }

    private val binding: MainActivityBinding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val navHeaderBinding: MainDrawerNavHeaderBinding by lazy {
        MainDrawerNavHeaderBinding.bind(binding.navView.getHeaderView(0))
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val fragmentIndex = intent.getIntExtra(KEY_FRAGMENT_INDEX, AppConfig.fragmentIndex)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Fragment 容器
        val hostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = hostFragment.navController

        // 关联抽屉菜单和Fragment
        appBarConfiguration = AppBarConfiguration(
            drawerMenus.toSet(), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        // 设置启动页面
        val checkedId = drawerMenus.getOrElse(fragmentIndex) { 0 }
        navView.setCheckedItem(checkedId)

        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(checkedId)
        navController.graph = navGraph

        navHeaderBinding.apply {
            subtitle.text = BuildConfig.VERSION_NAME
        }

        lifecycleScope.runOnIO {
            ShortCuts.buildShortCuts(this)
            if (AppConfig.isAutoCheckUpdateEnabled) {
                Log.d(TAG, "check for update...")
                MyTools.checkUpdate(this)
            }
        }

        importFileFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        importFileFromIntent(intent)
    }

    private fun importFileFromIntent(intent: Intent?) {
        if (intent?.data != null)
            MaterialAlertDialogBuilder(this)
                .setTitle("导入文件为...")
                .setItems(arrayOf("配置列表", "插件", "替换规则", "朗读规则")) { _, which ->
                    var fragment: BaseImportConfigBottomSheetFragment? = null
                    when (which) {
                        0 -> {
                            fragment =
                                com.github.jing332.tts_server_android.ui.systts.list.ImportConfigBottomSheetFragment()
                            fragment.fileUri = intent.data
                        }

                        1 -> {
                            fragment =
                                com.github.jing332.tts_server_android.ui.systts.plugin.ImportConfigBottomSheetFragment()
                            fragment.fileUri = intent.data
                        }

                        2 -> {
                            fragment =
                                com.github.jing332.tts_server_android.ui.systts.replace.ImportConfigBottomSheetFragment()
                            fragment.fileUri = intent.data
                        }

                        3 -> {
                            fragment =
                                com.github.jing332.tts_server_android.ui.systts.speech_rule.ImportConfigBottomSheetFragment()
                            fragment.fileUri = intent.data
                        }
                    }
                    intent.data = null
                    fragment?.show(supportFragmentManager, "BaseImportConfigBottomSheetFragment")
                }
                .setPositiveButton(R.string.cancel, null)
                .show()
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
        if (handled) {
            AppConfig.fragmentIndex = when (menuItem.itemId) {
                R.id.nav_systts -> INDEX_SYS_TTS
                R.id.nav_systts_forwarder -> INDEX_FORWARDER_SYS
                R.id.nav_server -> INDEX_FORWARDER_MS
                else -> 0
            }
        } else {
            when (menuItem.itemId) {
                R.id.nav_killBattery -> killBattery()
                R.id.nav_checkUpdate -> lifecycleScope.runOnIO {
                    MyTools.checkUpdate(
                        this,
                        isFromUser = true
                    )
                }

                R.id.nav_about -> displayAboutDialog()
            }
        }

        binding.navView.parent.let { if (it is DrawerLayout) it.closeDrawer(binding.navView) }

        return handled
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let { MenuCompat.setGroupDividerEnabled(it, true) }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
        return super.onPrepareOptionsMenu(menu)
    }


    private var lastBackDownTime = 0L
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            SystemClock.elapsedRealtime().let {
                if (it - lastBackDownTime <= 1500) {
                    finish()
                } else {
                    toast(getString(R.string.app_down_again_to_exit))
                    lastBackDownTime = it
                }
                return true
            }

        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Suppress("DEPRECATION")
    private fun displayAboutDialog() {
        val tv = TextView(this)
        tv.movementMethod = LinkMovementMethod()
        tv.text = Html.fromHtml(resources.openRawResource(R.raw.abort_info).readAllText())
        tv.gravity = Gravity.CENTER /* 居中 */
        tv.setPadding(25)
        MaterialAlertDialogBuilder(this).setTitle(R.string.about).setView(tv)
            .show()
    }

    @SuppressLint("BatteryLife")
    private fun killBattery() {
        val intent = Intent()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                toast(R.string.added_background_whitelist)
            } else {
                try {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    toast(R.string.system_not_support_please_manual_set)
                    e.printStackTrace()
                }
            }
        }
    }
}