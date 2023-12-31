package com.github.jing332.tts_server_android.compose.systts.plugin

internal sealed class NavRoutes(val id: String) {
    data object PluginManager : NavRoutes("plugin_manager")
    data object PluginEdit : NavRoutes("plugin_edit") {
        const val KEY_DATA = "data"
    }
}