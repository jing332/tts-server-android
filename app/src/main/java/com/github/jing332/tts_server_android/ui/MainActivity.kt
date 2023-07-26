package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.net.utils.fileName
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ShortCuts
import com.github.jing332.tts_server_android.constant.AppTheme
import com.github.jing332.tts_server_android.databinding.MainActivityBinding
import com.github.jing332.tts_server_android.databinding.MainDrawerNavHeaderBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory.newEditorFromJS
import com.github.jing332.tts_server_android.ui.view.ActivityTransitionHelper.initExitSharedTransition
import com.github.jing332.tts_server_android.ui.view.ThemeExtensions.initAppTheme
import com.github.jing332.tts_server_android.utils.*
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import splitties.systemservices.powerManager
import java.util.*


class MainActivity : AppCompatActivity(R.layout.main_activity),
    NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val TAG = "MainActivity"
        const val KEY_FRAGMENT_INDEX = "KEY_INDEX"

        const val INDEX_SYS_TTS = 0
        const val INDEX_FORWARDER_SYS = 1
        const val INDEX_FORWARDER_MS = 2

        private val drawerMenus by lazy {
            listOf(R.id.nav_systts, R.id.nav_systts_forwarder, R.id.nav_server, R.id.nav_settings)
        }
    }

    private val binding by viewBinding(MainActivityBinding::bind)
    private val navHeaderBinding: MainDrawerNavHeaderBinding by lazy {
        MainDrawerNavHeaderBinding.bind(binding.navView.getHeaderView(0))
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        initExitSharedTransition()
        initAppTheme()
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

        // 关联标题栏和Fragment
        appBarConfiguration = AppBarConfiguration(
            drawerMenus.toSet(), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 关联侧边栏和Fragment
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        // 设置启动页面
        val checkedId = drawerMenus.getOrElse(fragmentIndex) { 0 }
        navView.setCheckedItem(checkedId)

        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(checkedId)
        navController.graph = navGraph
        // 设置 抽屉导航按钮根据用户侧滑滑动变化
        navController.addOnDestinationChangedListener { _, _, _ ->
            ActionBarDrawerToggle(
                this,
                drawerLayout,
                binding.appBarMain.toolbar,
                R.string.nav_app_bar_open_drawer_description,
                R.string.nav_app_bar_open_drawer_description
            ).apply {
                drawerLayout.setDrawerListener(this)
                syncState()
            }
        }

        lifecycleScope.runOnIO {
            ShortCuts.buildShortCuts(this)
            if (AppConfig.isAutoCheckUpdateEnabled)
                MyTools.checkUpdate(this)
        }

        importConfigFromIntent(intent)

        initDrawerView()
        addBackPressedCallback()

        kotlin.runCatching {
            assets.open("help/app.md").use {
                it.bufferedReader().use { reader ->
                    val version = reader.readLine().split("-")[1]
                    if (version.toInt() > AppConfig.lastReadHelpDocumentVersion) {
                        startActivity(Intent(this, AppHelpDocumentActivity::class.java))
                        AppConfig.lastReadHelpDocumentVersion = version.toInt()
                    }
                }
            }
        }
    }

    // 抽屉View
    private fun initDrawerView() {
        navHeaderBinding.subtitle.text = BuildConfig.VERSION_NAME
        navHeaderBinding.btnTheme.clickWithThrottle {
            val scrollView = NestedScrollView(this)
            val chipGroup = ChipGroup(this).apply {
                setPadding(16.dp)
            }
            scrollView.addView(chipGroup)
            val dlg = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme)
                .setView(scrollView)
                .setNegativeButton(R.string.cancel, null)
                .show()
            AppTheme.values().forEach { theme ->
                Chip(this).apply {
                    val themeColor = ContextCompat.getColor(this@MainActivity, theme.colorId)
                    text = getString(theme.strId)
                    isCheckable = true
                    isChecked = theme == AppConfig.theme
                    chipStrokeColor =
                        ColorStateList.valueOf(if (isChecked) themeColor else Color.TRANSPARENT)
                    chipIcon = ColorDrawable(themeColor)
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                    chipGroup.addView(this)
                    setOnClickListener {
                        dlg.dismiss()
                        AppConfig.theme = theme
                        this@MainActivity.restart()
                    }
                }
            }
        }
    }

    private fun addBackPressedCallback() {
        var lastBackDownTime = 0L
        addOnBackPressed(this) {
            if (navController.currentDestination?.id == R.id.nav_settings) {
                if (navController.popBackStack()) {
                    binding.navView.setCheckedItem(
                        navController.currentDestination?.id ?: return@addOnBackPressed true
                    )
                    return@addOnBackPressed true
                }
            }

            SystemClock.elapsedRealtime().let {
                if (it - lastBackDownTime <= 1500) {
                    finish()
                } else {
                    toast(getString(R.string.app_down_again_to_exit))
                    lastBackDownTime = it
                    return@addOnBackPressed true
                }
            }
            false
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        importConfigFromIntent(intent)
    }

    private fun importConfigFromIntent(intent: Intent?) {
        intent?.data?.let {
            when (it.scheme) {
                ContentResolver.SCHEME_CONTENT -> importFileFromIntent(intent)
                ContentResolver.SCHEME_FILE -> importFileFromIntent(intent)
                "ttsrv" -> importUrlFromIntent(intent)
                else -> longToast(getString(R.string.invalid_scheme_msg))
            }
        }
    }

    private fun importUrlFromIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "ttsrv") {
                val path = uri.host ?: ""
                val url = uri.path?.removePrefix("/") ?: ""
                if (url.isBlank()) {
                    longToast(getString(R.string.invalid_url_msg, url))
                    intent.data = null
                    return
                }

                val fragment = ImportConfigFactory.createFragment(path)
                if (fragment == null) {
                    longToast(getString(R.string.invalid_path_msg, path))
                    intent.data = null
                    return
                }

                fragment.remoteUrl = url
                fragment.show(supportFragmentManager, "ImportConfigBottomSheetFragment")
                intent.data = null
            }
        }
    }

    private fun importFileFromIntent(intent: Intent?) {
        if (intent?.data != null) {
            if (intent.data?.fileName()?.endsWith("js", true) == true) {
                val txt = intent.data?.readAllText(this)
                if (txt.isNullOrBlank()) {
                    longToast(R.string.js_file_type_not_recognized)
                } else
                    if (!newEditorFromJS(txt))
                        longToast(R.string.js_file_type_not_recognized)

            } else {
                val list = ImportConfigFactory.localizedTypeList(this).toList()
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.import_file_as)
                    .setItems(list.map { it.second }.toTypedArray()) { _, which ->
                        val fragment = ImportConfigFactory.createFragment(list[which].first)
                        fragment?.fileUri = intent.data
                        fragment?.show(
                            supportFragmentManager,
                            "BaseImportConfigBottomSheetFragment"
                        )
                        intent.data = null
                    }
                    .setPositiveButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        var handled = false
        when (menuItem.itemId) {
            R.id.nav_killBattery -> killBattery()
            R.id.nav_checkUpdate -> lifecycleScope.runOnIO {
                MyTools.checkUpdate(
                    this,
                    isFromUser = true
                )
            }

            R.id.nav_help_document -> {
                startActivity(Intent(this, AppHelpDocumentActivity::class.java))
            }

            R.id.nav_about -> displayAboutDialog()
            else -> {
                handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) {
                    AppConfig.fragmentIndex = when (menuItem.itemId) {
                        R.id.nav_systts -> INDEX_SYS_TTS
                        R.id.nav_systts_forwarder -> INDEX_FORWARDER_SYS
                        R.id.nav_server -> INDEX_FORWARDER_MS
                        else -> 0
                    }
                }
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

    override fun onSupportNavigateUp(): Boolean {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
                toast(R.string.added_background_whitelist)
            } else {
                kotlin.runCatching {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    })
                }.onFailure {
                    toast(R.string.system_not_support_please_manual_set)
                }
            }
        }
    }
}