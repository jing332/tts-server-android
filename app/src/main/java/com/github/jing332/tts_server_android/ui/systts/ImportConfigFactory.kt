package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.content.Intent
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment

object ImportConfigFactory {
    private const val ID_LIST = "list"
    private const val ID_PLUGIN = "plugin"
    private const val ID_REPLACE_RULE = "replaceRule"
    private const val ID_SPEECH_RULE = "speechRule"

    private val typeList = linkedMapOf(
        ID_LIST to R.string.config_list,
        ID_PLUGIN to R.string.plugin,
        ID_REPLACE_RULE to R.string.replace_rule,
        ID_SPEECH_RULE to R.string.speech_rule
    )

    fun localizedTypeList(context: Context): List<Pair<String, String>> {
        return typeList.map { it.key to context.getString(it.value) }
    }

    fun createFragment(type: String): BaseImportConfigBottomSheetFragment? {
        return when (type) {
//            ID_LIST -> com.github.jing332.tts_server_android.ui.systts.list.ImportConfigBottomSheetFragment()
//            ID_PLUGIN -> com.github.jing332.tts_server_android.ui.systts.plugin.ImportBottomSheetFragment()
            ID_REPLACE_RULE -> com.github.jing332.tts_server_android.ui.systts.replace.ImportConfigBottomSheetFragment()
//            ID_SPEECH_RULE -> com.github.jing332.tts_server_android.ui.systts.speech_rule.ImportConfigBottomSheetFragment()
            else -> null
        }
    }

    /**
     * @return 是否识别成功
     */
    fun Context.newEditorFromJS(js: String): Boolean {
//        if (js.contains("PluginJS")) {
//            startActivity(Intent(this, PluginManagerActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("js", js)
//            })
//
//        } else if (js.contains("SpeechRuleJS")) {
//            startActivity(Intent(this, SpeechRuleManagerActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("js", js)
//            })
//        } else
            return false

//        return true
    }
}