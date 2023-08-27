package com.github.jing332.tts_server_android.compose.plugin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.jing332.tts_server_android.compose.ConfigImportBottomSheet
import com.github.jing332.tts_server_android.compose.ConfigModel
import com.github.jing332.tts_server_android.compose.SelectImportConfigDialog
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

@Composable
fun PluginImportBottomSheet(onDismissRequest: () -> Unit) {
    var list by remember { mutableStateOf<List<Plugin>?>(null) }
    if (list != null) {
        SelectImportConfigDialog(
            onDismissRequest = { list = null },
            models = list!!.map {
                ConfigModel(
                    isSelected = true,
                    title = it.name,
                    subtitle = it.author,
                    it
                )
            },
            onSelectedList = {
                appDb.pluginDao.insert(*it.map { plugin -> plugin as Plugin }.toTypedArray())

                it.size
            }
        )
    }

    ConfigImportBottomSheet(onDismissRequest = onDismissRequest, onImport = {
        list = AppConst.jsonBuilder.decodeFromString<List<Plugin>>(it)
    })
}