package com.github.jing332.tts_server_android.help

import com.chibatching.kotpref.KotprefModel

object ServerConfig : KotprefModel() {
    override val kotprefName: String
        get() = "server"

    /**
     * 端口
     */
    var port by intPref(1233)

    /**
     * 唤醒锁
     */
    var isWakeLockEnabled by booleanPref()

    /**
     * token
     */
    var token by stringPref()
}