package com.github.jing332.tts_server_android.ui.systts.speech_rule

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.help.config.SpeechRuleConfig
import com.github.jing332.tts_server_android.ui.systts.base.BaseScriptEditorActivity
import com.github.jing332.tts_server_android.ui.view.ActivityTransitionHelper.initTargetTransition
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.observeNoSticky

class SpeechRuleEditorActivity : BaseScriptEditorActivity() {
    private val vm: SpeechRuleEditorViewModel by viewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.onCreate(savedInstanceState)
        initTargetTransition()
        supportActionBar?.title = getString(R.string.speech_rule)

        vm.errorLiveData.observeNoSticky(this) {
            displayErrorDialog(it)
        }
        vm.displayLoggerLiveData.observeNoSticky(this) {
            displayDebugBottomSheet()
        }

        vm.codeLiveData.observe(this) {
            editor.setText(it)
        }

        val rule = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: SpeechRule()
        if (rule.name.isNotBlank()) supportActionBar?.subtitle = rule.name


        vm.init(rule, assets.open("defaultData/speech_rule.js").readAllText())
    }

    override fun updateCode(code: String) {
        vm.code = code
    }

    override fun clearCacheFile(): Boolean = true

    override fun onGetSaveFileName(): String = "ttsrv-${vm.speechRule.name}.js"

    override fun onSave(): Parcelable? {
        return if (vm.evalRuleInfo())
            vm.speechRule
        else null
    }

    override fun getLogger() = vm.logger
    override fun onDebug() {
        vm.debug(SpeechRuleConfig.textParam)
    }


    override fun onScriptSyncAction(name: String, body: ByteArray?) {}
    override fun onScriptSyncPush() {}

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu).apply {
            menuInflater.inflate(R.menu.systts_speech_rule_editor, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_text_params -> {
                AppDialogs.displayInputDialog(
                    this,
                    getString(R.string.set_sample_text_param),
                    text = SpeechRuleConfig.textParam
                ) {
                    SpeechRuleConfig.textParam = it
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}