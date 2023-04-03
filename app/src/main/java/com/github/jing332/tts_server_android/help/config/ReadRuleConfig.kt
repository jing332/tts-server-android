package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel

object ReadRuleConfig : KotprefModel() {
    override val kotprefName: String = "read_rule"

    var textParam by stringPref("")
}