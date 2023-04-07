package com.github.jing332.tts_server_android.ui.systts.plugin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.base.import1.ConfigItemModel
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast
import kotlinx.serialization.decodeFromString

class ImportConfigBottomSheetFragment : BaseImportConfigBottomSheetFragment(R.string.import_plugin) {
    companion object {
        const val TAG = "ImportConfigBottomSheetFragment"
    }

    override fun onImport(json: String) {
        val list: List<Plugin> = App.jsonBuilder.decodeFromString(json)
        displayListSelectDialog(list.map { ConfigItemModel(true, it.name, it.author, it) }) {
            appDb.pluginDao.insert(*it.map { plugin -> plugin as Plugin }.toTypedArray())

            longToast(getString(R.string.config_import_success_msg, it.size))
            dismiss()
        }
    }

    override fun checkJson(json: String)= json.contains("pluginId")

}