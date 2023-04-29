package com.github.jing332.tts_server_android.ui.systts.plugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.config.PluginConfig
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.base.BaseScriptEditorActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.ActivityTransitionHelper.initTargetTransition
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.observeNoSticky
import com.github.jing332.tts_server_android.utils.toast


class PluginEditorActivity : BaseScriptEditorActivity() {
    private val vm: PluginEditorViewModel by viewModels()

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)?.let {
                toast("参数仅本次编辑生效")
                vm.updateTTS(it.tts as PluginTTS)
            }
        }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.onCreate(savedInstanceState)
        initTargetTransition()
        supportActionBar?.title = getString(R.string.plugin)

        vm.init(
            plugin = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: Plugin(),
            defaultCode = resources.assets.open("defaultData/plugin-azure.js").readAllText()
        )
        supportActionBar?.subtitle = vm.pluginInfo.name

        vm.codeLiveData.observe(this) {
            editor.setText(it)
        }

        vm.displayLoggerLiveData.observeNoSticky(this) {
            displayDebugBottomSheet()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu).apply {
            menuInflater.inflate(R.menu.systts_plugin_editor, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_set_sample_text -> {
                AppDialogs.displayInputDialog(
                    this,
                    getString(R.string.set_sample_text_param),
                    getString(R.string.sample_text),
                    PluginConfig.sampleText
                ) {
                    PluginConfig.sampleText = it
                }
            }

            R.id.menu_text_params -> previewUi()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onScriptSyncAction(name: String, body: ByteArray?) {
        when (name) {
            "ui" -> previewUi()
        }
    }

    override fun onScriptSyncPush() {
        AppConst.localBroadcast.sendBroadcast(Intent(PluginTtsEditActivity.ACTION_FINISH))
    }

    override fun updateCode(code: String) {
        vm.updatePluginCode(code)
    }

    override fun clearCacheFile(): Boolean {
        vm.clearPluginCache()
        toast(R.string.cleared)
        return true
    }

    override fun onGetSaveFileName(): String = "ttsrv-${vm.pluginInfo.name}.js"

    override fun onSave(): Parcelable? {
        val plugin = try {
            vm.pluginEngine.evalPluginInfo()
        } catch (e: Exception) {
            displayErrorDialog(e)
            return null
        }
        return vm.pluginInfo
    }

    override fun getLogger(): Logger = vm.pluginEngine.logger
    override fun onDebug() {
        vm.debug()
    }

    private fun previewUi() {
        vm.updatePluginCode(editor.text.toString(), isSave = true)
        startForResult.launch(Intent(this, PluginTtsEditActivity::class.java).apply {
            putExtra(BaseTtsEditActivity.KEY_BASIC_VISIBLE, false)
            putExtra(BaseTtsEditActivity.KEY_DATA, SystemTts(tts = vm.pluginTTS))
        })
    }


}