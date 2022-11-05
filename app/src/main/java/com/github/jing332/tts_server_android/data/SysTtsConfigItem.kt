package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import java.io.Serializable

data class SysTtsConfigItem(
    var uiData: TtsConfigListItemData,
    var isEnabled: Boolean,
    @ReadAloudTarget var readAloudTarget: Int,
    var locale: String,
    var voiceProperty: VoiceProperty,
    var format: String,
//    var voiceName: String,
//    var voiceStyle: String,
//    var voiceStyleDegree: Int,
//    var voiceRole: String,
//    var voiceId: String,
//    var volume: Int,
//    var rate: Int,
) : Serializable {
    constructor() : this(
        TtsConfigListItemData(), false,
        ReadAloudTarget.DEFAULT, "zh-CN",
        VoiceProperty("zh-CN-XiaoxiaoNeural"), ""
    )


//    /* 转为百分比字符串 */
//    fun volumeToPctString(): String {
//        return "${volume - 50}%"
//    }
//
//    fun rateToPcmString(): String {
//        return "${(rate - 50) * 2}%"
//    }

//    fun toVoiceProperty(pitch: String): VoiceProperty {
//        return VoiceProperty(voiceName, Prosody(rateToPcmString(), volumeToPctString(), pitch))
//    }
}