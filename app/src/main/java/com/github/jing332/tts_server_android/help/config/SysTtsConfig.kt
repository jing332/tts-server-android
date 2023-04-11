package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel

object SysTtsConfig : KotprefModel() {
    override val kotprefName: String
        get() = "systts"

    /**
     * 是否 应用内播放音频
     */
    var isInAppPlayAudio by booleanPref()

    /**
     * APP内播放语速
     */
    var inAppPlaySpeed by floatPref(1F)

    /**
     * APP内播放音调
     */
    var inAppPlayPitch by floatPref(1F)

    var bgmVolume by floatPref(1f)
    var isBgmShuffleEnabled by booleanPref(false)

    /**
     * 多语音
     */
    var isMultiVoiceEnabled by booleanPref()

    /**
     * 相同朗读目标可多选
     */
    var isVoiceMultipleEnabled by booleanPref()

    /**
     * 分组可多选
     */
    var isGroupMultipleEnabled by booleanPref()

    var isWakeLockEnabled by booleanPref(true)


    var isForegroundServiceEnabled by booleanPref(true)

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

    var maxRetryCount by intPref(3)

    /**
     * 备用配置在第几次重试时触发
     */
    var standbyTriggeredRetryIndex by intPref(1)

    var isStreamPlayModeEnabled by booleanPref(false)
}