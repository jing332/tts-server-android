package com.github.jing332.tts_server_android.ui.systts.read_rule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.entities.ReadRule
import com.github.jing332.tts_server_android.help.config.ReadRuleConfig
import com.github.jing332.tts_server_android.ui.systts.BaseScriptEditorActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.observeNoSticky

class ReadRuleEditorActivity : BaseScriptEditorActivity() {
    private val vm: ReadRuleEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.errorLiveData.observeNoSticky(this) {
            displayErrorDialog(it)
        }
        vm.displayLoggerLiveData.observeNoSticky(this) {
            displayDebugBottomSheet()
        }

        vm.codeLiveData.observe(this) {
            editor.setText(it)
        }

        val rule = intent.getParcelableExtra<ReadRule>(KeyConst.KEY_DATA) ?: ReadRule()
        vm.init(rule, assets.open("defaultData/read_rule.js").readAllText())
    }

    override fun updateCode(code: String) {
        vm.code = code
    }

    override fun clearPluginCache(): Boolean = true

    override fun onSaveAsFile(): String = ""


    override fun onSave(): Boolean {
        return vm.evalRuleInfo()
    }

    override fun getResultData() = vm.readRule

    override fun getLogger() = vm.logger
    override fun onDebug() {
        vm.debug(ReadRuleConfig.textParam)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu).apply {
            (menu as MenuBuilder).setOptionalIconsVisible(true)
            MenuCompat.setGroupDividerEnabled(menu, true)
            menuInflater.inflate(R.menu.systts_read_rule_editor, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_params -> {
                AppDialogs.displayInputDialog(
                    this,
                    getString(R.string.set_sample_text_param),
                    ReadRuleConfig.textParam
                ) {
                    ReadRuleConfig.textParam = it
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}