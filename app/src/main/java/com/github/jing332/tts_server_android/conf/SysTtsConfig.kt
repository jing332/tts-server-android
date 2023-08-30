package com.github.jing332.tts_server_android.conf

object SysTtsConfig {
    var isInAppPlayAudio: Boolean
        get() = SystemTtsConfig.isInternalPlayerEnabled.value
        set(value) {
            SystemTtsConfig.isInternalPlayerEnabled.value = value
        }

    var inAppPlaySpeed: Float
        get() = SystemTtsConfig.inAppPlaySpeed.value
        set(value) {
            SystemTtsConfig.inAppPlaySpeed.value = value
        }

    var inAppPlayVolume: Float
        get() = SystemTtsConfig.inAppPlayVolume.value
        set(value) {
            SystemTtsConfig.inAppPlayVolume.value = value
        }

    var inAppPlayPitch: Float
        get() = SystemTtsConfig.inAppPlayPitch.value
        set(value) {
            SystemTtsConfig.inAppPlayPitch.value = value
        }

    var audioParamsSpeed: Float
        get() = SystemTtsConfig.audioParamsSpeed.value
        set(value) {
            SystemTtsConfig.audioParamsSpeed.value = value
        }

    var audioParamsPitch: Float
        get() = SystemTtsConfig.audioParamsPitch.value
        set(value) {
            SystemTtsConfig.audioParamsPitch.value = value
        }

    var audioParamsVolume: Float
        get() = SystemTtsConfig.audioParamsVolume.value
        set(value) {
            SystemTtsConfig.audioParamsVolume.value = value
        }

    var bgmVolume: Float
        get() = SystemTtsConfig.bgmVolume.value
        set(value) {
            SystemTtsConfig.bgmVolume.value = value
        }

    var isBgmShuffleEnabled: Boolean
        get() = SystemTtsConfig.isBgmShuffleEnabled.value
        set(value) {
            SystemTtsConfig.isBgmShuffleEnabled.value = value
        }

    var isMultiVoiceEnabled: Boolean
        get() = SystemTtsConfig.isMultiVoiceEnabled.value
        set(value) {
            SystemTtsConfig.isMultiVoiceEnabled.value = value
        }

    var isWakeLockEnabled: Boolean
        get() = SystemTtsConfig.isWakeLockEnabled.value
        set(value) {
            SystemTtsConfig.isWakeLockEnabled.value = value
        }

    var isForegroundServiceEnabled: Boolean
        get() = SystemTtsConfig.isForegroundServiceEnabled.value
        set(value) {
            SystemTtsConfig.isForegroundServiceEnabled.value = value
        }

    var isReplaceEnabled: Boolean
        get() = SystemTtsConfig.isReplaceEnabled.value
        set(value) {
            SystemTtsConfig.isReplaceEnabled.value = value
        }

    var isSplitEnabled: Boolean
        get() = SystemTtsConfig.isSplitEnabled.value
        set(value) {
            SystemTtsConfig.isSplitEnabled.value = value
        }

    var requestTimeout: Int
        get() = SystemTtsConfig.requestTimeout.value
        set(value) {
            SystemTtsConfig.requestTimeout.value = value
        }

    var maxRetryCount: Int
        get() = SystemTtsConfig.maxRetryCount.value
        set(value) {
            SystemTtsConfig.maxRetryCount.value = value
        }

    var standbyTriggeredRetryIndex: Int
        get() = SystemTtsConfig.standbyTriggeredRetryIndex.value
        set(value) {
            SystemTtsConfig.standbyTriggeredRetryIndex.value = value
        }

    var maxEmptyAudioRetryCount: Int
        get() = SystemTtsConfig.maxEmptyAudioRetryCount.value
        set(value) {
            SystemTtsConfig.maxEmptyAudioRetryCount.value = value
        }

    var isSkipSilentText: Boolean
        get() = SystemTtsConfig.isSkipSilentText.value
        set(value) {
            SystemTtsConfig.isSkipSilentText.value = value
        }

    var isStreamPlayModeEnabled: Boolean
        get() = SystemTtsConfig.isStreamPlayModeEnabled.value
        set(value) {
            SystemTtsConfig.isStreamPlayModeEnabled.value = value
        }

    var isExoDecoderEnabled: Boolean
        get() = SystemTtsConfig.isExoDecoderEnabled.value
        set(value) {
            SystemTtsConfig.isExoDecoderEnabled.value = value
        }
}