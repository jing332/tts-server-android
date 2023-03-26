package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel

object PluginConfig : KotprefModel() {
    override val kotprefName: String
        get() = "plugin"

    var editorTheme by intPref(EditorTheme.QUIET_LIGHT)

    var editorWordWrapEnabled by booleanPref(false)

    object EditorTheme {
        const val AUTO = 0
        const val QUIET_LIGHT = 1
        const val SOLARIZED_DRAK = 2
        const val DARCULA = 3
        const val ABYSS = 4
//        const val GITHUB = 4
//        const val VS2019 = 5
//        const val ECLIPSE = 6
//        const val NOTEPADXX = 7
    }
}