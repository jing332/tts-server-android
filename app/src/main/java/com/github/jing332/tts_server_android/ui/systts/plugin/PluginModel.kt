package com.github.jing332.tts_server_android.ui.systts.plugin

import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

data class PluginModel(val data: Plugin) {
    val title: String
        get() = data.name

    val subTitle: String
        get() = "${data.author} - v${data.version}"

}
