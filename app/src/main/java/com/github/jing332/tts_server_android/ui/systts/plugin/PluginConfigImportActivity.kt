package com.github.jing332.tts_server_android.ui.systts.plugin

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.ui.base.import1.BaseConfigImportActivity
import com.github.jing332.tts_server_android.ui.base.import1.ConfigImportItemModel
import kotlinx.serialization.decodeFromString

class PluginConfigImportActivity : BaseConfigImportActivity() {
    override fun onImport(json: String) {
        val list: List<Plugin> = App.jsonBuilder.decodeFromString(json)
        displayListSelectDialog(list.map { ConfigImportItemModel(true, it.name, it.author, it) }) {
            it.forEach { plugin ->
                appDb.pluginDao.insert(plugin as Plugin)
            }
            finish()
        }
    }
}