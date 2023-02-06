package com.github.jing332.tts_server_android.ui.systts.plugin

import androidx.lifecycle.ViewModel
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class PluginManagerViewModel : ViewModel() {
    fun importConfig(json: String): String? {
        kotlin.runCatching {
            val plugins: List<Plugin> = App.jsonBuilder.decodeFromString(json)
            plugins.forEach {
                appDb.pluginDao.insert(it)
            }
        }.onFailure {
            return it.message ?: it.cause?.message
        }
        return null
    }

    fun importConfigFromUrl(url: String): String? {
        kotlin.runCatching {
            val json = Net.get(url).execute<String>()
            importConfig(json)
        }.onFailure {
            return it.message ?: it.cause?.message
        }
        return null
    }

    fun exportConfig(): String {
        return App.jsonBuilder.encodeToString(appDb.pluginDao.all)
    }
}