package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.content.Intent
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.ui.systts.speech_rule.SpeechRuleManagerActivity

object ImportConfigFactory {
    val typeList = linkedMapOf(
        "list" to R.string.config_list,
        "plugin" to R.string.plugin,
        "replaceRule" to R.string.replace_rule,
        "speechRule" to R.string.speech_rule
    )

    fun localizedTypeList(context: Context): List<Pair<String, String>> {
        return typeList.map { it.key to context.getString(it.value) }
    }

    fun createFragment(type: String): BaseImportConfigBottomSheetFragment? {
        return when (type) {
            "list" -> com.github.jing332.tts_server_android.ui.systts.list.ImportConfigBottomSheetFragment()
            "plugin" -> com.github.jing332.tts_server_android.ui.systts.plugin.ImportBottomSheetFragment()
            "replaceRule" -> com.github.jing332.tts_server_android.ui.systts.replace.ImportConfigBottomSheetFragment()
            "speechRule" -> com.github.jing332.tts_server_android.ui.systts.speech_rule.ImportConfigBottomSheetFragment()
            else -> null
        }
    }

    /**
     * @return 是否识别成功
     */
    fun Context.newEditorFromJS(js: String): Boolean {
        if (js.contains("PluginJS")) {
            startActivity(Intent(this, PluginManagerActivity::class.java).apply {
                putExtra("js", js)
            })

        } else if (js.contains("SpeechRuleJS")) {
            startActivity(Intent(this, SpeechRuleManagerActivity::class.java).apply {
                putExtra("js", js)
            })
        } else
            return false

        return true
    }
}