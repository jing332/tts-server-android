package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.service.systts.help.TtsAudioFormat
import java.io.Serializable

@kotlinx.serialization.Serializable
data class SysTtsConfigItem(
    var uiData: TtsConfigListItemData,
    var isEnabled: Boolean,
    @ReadAloudTarget var readAloudTarget: Int,
    var voiceProperty: VoiceProperty,
    var format: String,
) : Serializable {
    constructor() : this(
        TtsConfigListItemData(), false,
        ReadAloudTarget.DEFAULT,
        VoiceProperty(), TtsAudioFormat.DEFAULT
    )

    constructor(isEnabled: Boolean, @ReadAloudTarget readAloudTarget: Int) : this(
        TtsConfigListItemData(), isEnabled,
        readAloudTarget,
        VoiceProperty(), TtsAudioFormat.DEFAULT
    )

    var locale: String
        inline get() = voiceProperty.locale
        inline set(value) {
            voiceProperty.locale = value
        }
}