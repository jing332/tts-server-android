package com.github.jing332.tts_server_android.ui.systts.speech_rule

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.base.import1.ConfigItemModel
import com.github.jing332.tts_server_android.util.longToast
import kotlinx.serialization.decodeFromString

class ImportConfigBottomSheetFragment : BaseImportConfigBottomSheetFragment() {
    companion object {
        const val TAG = "ImportConfigBottomSheetFragment"
    }

    override fun onImport(json: String) {
        val list: List<SpeechRule> = App.jsonBuilder.decodeFromString(json)
        displayListSelectDialog(list.map { ConfigItemModel(true, it.name, it.author, it) }) {
            appDb.speechRule.insert(*it.map { rule -> rule as SpeechRule }.toTypedArray())

            longToast(getString(R.string.config_import_success_msg, it.size))
            dismiss()
        }
    }

    override fun checkJson(json: String) = json.contains("ruleId")
}