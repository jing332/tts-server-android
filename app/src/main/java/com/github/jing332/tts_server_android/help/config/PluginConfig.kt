package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel
import com.github.jing332.tts_server_android.constant.CodeEditorTheme

object PluginConfig : KotprefModel() {
    override val kotprefName: String
        get() = "plugin"

    var editorTheme by intPref(CodeEditorTheme.QUIET_LIGHT)

    var editorWordWrapEnabled by booleanPref(false)

    var isRemoteSyncEnabled by booleanPref(false)

    var remoteSyncPort by intPref(4566)
}