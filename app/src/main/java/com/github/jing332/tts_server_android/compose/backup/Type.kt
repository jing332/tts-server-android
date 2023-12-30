package com.github.jing332.tts_server_android.compose.backup

import com.github.jing332.tts_server_android.R


sealed class Type(val nameStrId: Int) {
    companion object {
        val typeList by lazy {
            listOf(
                Preference,
                List,
                SpeechRule,
                ReplaceRule,
                Plugin,
                PluginVars
            )
        }
    }

    data object Preference : Type(R.string.preference_settings)
    data object List : Type(R.string.config_list)
    data object SpeechRule : Type(R.string.speech_rule)
    data object ReplaceRule : Type(R.string.replace_rule)

    abstract class IPlugin(val id: Int, val includeVars: Boolean) : Type(id)
    object Plugin : IPlugin(R.string.plugin, false)
    object PluginVars : IPlugin(R.string.plugin_vars, true)
}