package com.github.jing332.tts_server_android.help

import com.chibatching.kotpref.KotprefModel

object SysTtsConfig : KotprefModel() {
    override val kotprefName: String
        get() = "systts"

    /**
     * 多语音
     */
    var isMultiVoiceEnabled by booleanPref()

    /**
     * 替换
     */
    var isReplaceEnabled by booleanPref()

    /**
     * 分割长句
     */
    var isSplitEnabled by booleanPref()

    /**
     * 请求超时(毫秒)
     */
    var requestTimeout by intPref(5000)

    /**
     * 最小对话文本长度
     */
    var minDialogueLength by intPref()
}

