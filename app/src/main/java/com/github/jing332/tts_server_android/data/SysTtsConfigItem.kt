package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.systts.help.ssml.Prosody
import com.github.jing332.tts_server_android.service.systts.help.ssml.VoiceProperty
import java.io.Serializable

data class SysTtsConfigItem(
    var uiData: TtsConfigListItemData,
    var isEnabled: Boolean,
    @ReadAloudTarget var readAloudTarget: Int,
    var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceStyle: String,
    var voiceStyleDegree: Int,
    var voiceRole: String,
    var voiceId: String,
    var format: String,
    var volume: Int,
    var rate: Int,
) : Serializable {
    constructor() : this(
        TtsConfigListItemData(), false,
        ReadAloudTarget.DEFAULT, TtsApiType.EDGE, "zh-CN",
        "zh-CN-XiaoxiaoNeural", "", 100, "", "", "", 50, 50
    )

    /* 转为百分比字符串 */
    fun volumeToPctString(): String {
        return "${volume - 50}%"
    }

    fun rateToPcmString(): String {
        return "${(rate - 50) * 2}%"
    }

    fun toVoiceProperty(pitch: String): VoiceProperty {
        return VoiceProperty(voiceName, Prosody(rateToPcmString(), volumeToPctString(), pitch))
    }
}