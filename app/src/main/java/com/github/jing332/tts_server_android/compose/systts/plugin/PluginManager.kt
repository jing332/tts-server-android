package com.github.jing332.tts_server_android.compose.systts.plugin

import com.github.jing332.database.entities.plugin.Plugin
import com.github.jing332.tts_server_android.constant.AppConst
import java.io.File

class PluginManager(private val plugin: Plugin) {
    private val cacheDir = File(AppConst.externalFilesDir.absolutePath + "/plugin/${plugin.pluginId}")
    fun hasCache(): Boolean {
        return try {
            cacheDir.list()?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    fun clearCache() {
        try {
            cacheDir.deleteRecursively()
        } catch (_: Exception) {
        }
    }
}