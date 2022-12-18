package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityMainBinding
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.help.ServerConfig
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.MyTools
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val ACTION_OPTION_ITEM_SELECTED_ID = "ACTION_OPTION_ITEM_SELECTED_ID"
        const val KEY_MENU_ITEM_ID = "KEY_MENU_ITEM_ID"

        const val ACTION_BACK_KEY_DOWN = "ACTION_BACK_KEY_DOWN"
    }

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Fragment 容器
        val hostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = hostFragment.navController

        // 关联抽屉菜单和Fragment
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_systts, R.id.nav_server, R.id.nav_settings), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener(this)

        /* 版本名 */
        val tvVersion =
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.nav_header_subtitle)
        tvVersion.text = BuildConfig.VERSION_NAME

        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(
            if (AppConfig.fragmentIndex == 1) {
                R.id.nav_server
            } else {
                R.id.nav_systts
            }
        )
        navController.graph = navGraph

        MyTools.checkUpdate(this)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
        if (handled) {
            invalidateOptionsMenu()
            AppConfig.fragmentIndex = when (menuItem.itemId) {
                R.id.nav_systts -> 0
                R.id.nav_server -> 1
                else -> 0
            }
        } else {
            when (menuItem.itemId) {
                R.id.nav_killBattery -> killBattery()
                R.id.nav_checkUpdate -> MyTools.checkUpdate(this)
                R.id.nav_about -> showAboutDialog()
            }
        }

        binding.navView.parent.let { if (it is DrawerLayout) it.closeDrawer(binding.navView) }

        return handled
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        val id = when (navController.currentDestination?.id) {
            R.id.nav_systts -> R.menu.menu_systts
            R.id.nav_server -> R.menu.menu_server
            else -> return false
        }
        menuInflater.inflate(id, menu)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menu?.apply {
            SysTtsConfig.apply {
                when (navController.currentDestination?.id) {
                    R.id.nav_systts -> {
                        findItem(R.id.menu_isMultiVoice)?.isChecked = isMultiVoiceEnabled
                        findItem(R.id.menu_doSplit)?.isChecked = isSplitEnabled
                        findItem(R.id.menu_replace_manager)?.isChecked = isReplaceEnabled
                        findItem(R.id.menu_isInAppPlayAudio)?.isChecked = isInAppPlayAudio
                        findItem(R.id.menu_voiceMultiple)?.isChecked = isVoiceMultipleEnabled
                        findItem(R.id.menu_groupMultiple)?.isChecked = isGroupMultipleEnabled
                    }
                    R.id.nav_server -> {
                        findItem(R.id.menu_wakeLock)?.isChecked = ServerConfig.isWakeLockEnabled
                    }
                }
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        App.localBroadcast.sendBroadcast(Intent(ACTION_OPTION_ITEM_SELECTED_ID).apply {
            putExtra(KEY_MENU_ITEM_ID, item.itemId)
        })
        return super.onOptionsItemSelected(item)
    }

    var lastBackDownTime = 0L
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            System.currentTimeMillis().let {
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
    private fun showAboutDialog() {
        val tv = TextView(this)
        tv.movementMethod = LinkMovementMethod()
        tv.text = Html.fromHtml(resources.openRawResource(R.raw.abort_info).readAllText())
        tv.gravity = Gravity.CENTER /* 居中 */
        tv.setPadding(25, 25, 25, 25)
        AlertDialog.Builder(this).setTitle(R.string.about).setView(tv)
            .setMessage("本应用界面使用Kotlin开发，底层服务由Go开发.")
            .setFadeAnim().show()
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