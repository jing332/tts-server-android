package com.github.jing332.tts_server_android.help.config

import android.content.Context
import com.chibatching.kotpref.KotprefModel

object SysTtsForwarderConfig : KotprefModel() {

    override val kotprefName: String
        get() = "systts_forwarder"

    var port by intPref(1221)

    var isWakeLockEnabled by booleanPref(false)
}