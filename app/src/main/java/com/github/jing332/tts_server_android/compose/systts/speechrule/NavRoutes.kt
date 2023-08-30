package com.github.jing332.tts_server_android.compose.systts.speechrule

internal sealed class NavRoutes(val id: String) {
    data object SpeechRuleManager : NavRoutes("manager")
    data object SpeechRuleEdit : NavRoutes("edit") {
        const val KEY_DATA = "data"
    }
}