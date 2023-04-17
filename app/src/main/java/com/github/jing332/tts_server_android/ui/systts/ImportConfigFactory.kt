package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment

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
}