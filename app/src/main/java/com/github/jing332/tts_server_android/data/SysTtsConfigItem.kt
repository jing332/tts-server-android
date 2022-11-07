package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.service.systts.help.TtsAudioFormat
import java.io.Serializable

data class SysTtsConfigItem(
    var uiData: TtsConfigListItemData,
    var isEnabled: Boolean,
    @ReadAloudTarget var readAloudTarget: Int,
    var locale: String,
    var voiceProperty: VoiceProperty,
    var format: String,
) : Serializable {
    constructor() : this(
        TtsConfigListItemData(), false,
        ReadAloudTarget.DEFAULT, "zh-CN",
        VoiceProperty("zh-CN-XiaoxiaoNeural"), ""
    )

    constructor(isEnabled: Boolean, @ReadAloudTarget readAloudTarget: Int) : this(
        TtsConfigListItemData(), isEnabled,
        readAloudTarget, "zh-CN",
        VoiceProperty("zh-CN-XiaoxiaoNeural"), TtsAudioFormat.DEFAULT
    )
}