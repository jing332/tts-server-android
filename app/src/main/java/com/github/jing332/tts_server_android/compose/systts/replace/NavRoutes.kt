package com.github.jing332.tts_server_android.compose.systts.replace

internal sealed class NavRoutes(val id: String) {
    data object Manager : NavRoutes("manager")
    data object Edit : NavRoutes("edit") {
        const val KEY_DATA = "data"
    }
}