package com.github.jing332.tts_server_android.help.config

import android.content.Context
import com.chibatching.kotpref.KotprefModel

object SpeechRuleConfig : KotprefModel() {
    override val kotprefMode: Int
        get() = Context.MODE_MULTI_PROCESS
    override val kotprefName: String = "speech_rule"

    var textParam by stringPref("")
}