package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.net.utils.fileName
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.MainActivityBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory.newEditorFromJS
import com.github.jing332.tts_server_android.ui.view.ActivityTransitionHelper.initExitSharedTransition
import com.github.jing332.tts_server_android.ui.view.ThemeExtensions.initAppTheme
import com.github.jing332.tts_server_android.utils.*
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import splitties.systemservices.powerManager
import java.util.*


class MainActivity : AppCompatActivity(R.layout.main_activity) {
    companion object {
        const val TAG = "MainActivity"
        const val KEY_FRAGMENT_INDEX = "KEY_INDEX"

        const val INDEX_SYS_TTS = 0
        const val INDEX_FORWARDER_SYS = 1
        const val INDEX_FORWARDER_MS = 2


    }

    private val binding by viewBinding(MainActivityBinding::bind)


    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        initExitSharedTransition()
        initAppTheme()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

//        val fragmentIndex = intent.getIntExtra(KEY_FRAGMENT_INDEX, AppConfig.fragmentIndex)
        importConfigFromIntent(intent)
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