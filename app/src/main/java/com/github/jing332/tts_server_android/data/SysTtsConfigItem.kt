package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.service.systts.help.TtsAudioFormat
import java.io.Serializable

@kotlinx.serialization.Serializable
data class SysTtsConfigItem(
    var uiData: TtsConfigListItemData, /* UI显示数据 */
    var isEnabled: Boolean,  /* 是否启用 */
    @ReadAloudTarget var readAloudTarget: Int,
    var voiceProperty: VoiceProperty, /* 朗读属性 */
    var format: String, /* 音频格式 */
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
}