package com.github.jing332.tts_server_android.help.config

import android.content.Context
import com.chibatching.kotpref.KotprefModel
import com.github.jing332.tts_server_android.constant.CodeEditorTheme

object PluginConfig : KotprefModel() {

    override val kotprefName: String
        get() = "plugin"

                var sampleText by stringPref("示例文本。 Sample text.")
}