package com.github.jing332.tts_server_android.help

import com.chibatching.kotpref.KotprefModel

object AppConfig : KotprefModel() {
    override val kotprefName: String
        get() = "app"

    /**
     * 是否 Edge接口使用DNS解析IP
     */
    var isEdgeDnsEnabled by booleanPref()

    var testSampleText by stringPref()

    var fragmentIndex by intPref(0)
}