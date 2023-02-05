package com.github.jing332.tts_server_android.ui.systts.plugin

import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

data class PluginModel(val data: Plugin) {
    val name: String
        get() = data.name

}
