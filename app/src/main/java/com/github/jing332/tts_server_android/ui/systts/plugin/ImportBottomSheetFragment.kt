package com.github.jing332.tts_server_android.ui.systts.plugin

import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.ConfigType
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.base.import1.ConfigItemModel
import com.github.jing332.tts_server_android.utils.longToast
import kotlinx.serialization.decodeFromString

class ImportBottomSheetFragment :
    BaseImportConfigBottomSheetFragment(R.string.import_plugin, ConfigType.PLUGIN) {
    companion object {
        const val TAG = "ImportConfigBottomSheetFragment"
    }

    override fun onImport(json: String) {
        val list: List<Plugin> = AppConst.jsonBuilder.decodeFromString(json)
        displayListSelectDialog(list.map { ConfigItemModel(true, it.name, it.author, it) }) {
            appDb.pluginDao.insert(*it.map { plugin -> plugin as Plugin }.toTypedArray())

            longToast(getString(R.string.config_import_success_msg, it.size))
            dismiss()
        }
    }
}