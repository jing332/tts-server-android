package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel

object SysTtsConfig : KotprefModel() {
    override val kotprefName: String
        get() = "systts"

    var isInAppPlayAudio by booleanPref(false)
    var inAppPlaySpeed by floatPref(1f)
    var inAppPlayVolume by floatPref(1f)
    var inAppPlayPitch by floatPref(1f)

    var audioParamsSpeed by floatPref(1f)
    var audioParamsPitch by floatPref(1f)
    var audioParamsVolume by floatPref(1f)

    var bgmVolume by floatPref(1f)
    var isBgmShuffleEnabled by booleanPref(false)

    var isMultiVoiceEnabled by booleanPref()

    var isVoiceMultipleEnabled by booleanPref()
    var isGroupMultipleEnabled by booleanPref()

    var isWakeLockEnabled by booleanPref(true)
    var isForegroundServiceEnabled by booleanPref(true)

    var isReplaceEnabled by booleanPref()
    var isSplitEnabled by booleanPref()

    var requestTimeout by intPref(5000)
    var maxRetryCount by intPref(3)

    var standbyTriggeredRetryIndex by intPref(1)
    var maxEmptyAudioRetryCount by intPref(1)

    var isSkipSilentText by booleanPref(true)
    var isStreamPlayModeEnabled by booleanPref(false)
    var isExoDecoderEnabled by booleanPref(false)
}