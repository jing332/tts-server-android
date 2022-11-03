package com.github.jing332.tts_server_android.service.tts.help

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.help.ssml.Prosody
import com.github.jing332.tts_server_android.service.tts.help.ssml.VoiceProperty
import com.github.jing332.tts_server_android.util.FileUtils
import java.io.Serializable

data class TtsConfig(
    var list: List<ConfigItem>,
    var currentSelected: Int,
    var isSplitSentences: Boolean,
    var isMultiVoice: Boolean
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts.dat" }
        fun read(): TtsConfig {
            return FileUtils.readObject<TtsConfig>(filepath) ?: TtsConfig()
        }
    }

    constructor() : this(
        listOf(
            ConfigItem(
                ReadAloudTarget.DEFAULT,
                TtsApiType.CREATION,
                "zh-CN",
                "zh-CN-XiaoxiaoNeural",
                "", 100, "",
                "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
                "audio-24khz-48kbitrate-mono-mp3",
                50, 0
            ), ConfigItem(
                ReadAloudTarget.ASIDE,
                TtsApiType.EDGE,
                "zh-CN",
                "zh-CN-YunxiNeural",
                "", 100, "",
                "",
                "audio-24khz-48kbitrate-mono-mp3",
                50, 0
            ), ConfigItem(
                ReadAloudTarget.DIALOGUE,
                TtsApiType.EDGE,
                "zh-CN",
                "zh-CN-XiaoxiaoNeural",
                "", 100, "",
                "",
                "audio-24khz-48kbitrate-mono-mp3",
                50, 0
            )
        ), 0, false, false
    )

    fun save() {
        FileUtils.saveObject(this, filepath)
    }

    fun selectedItem(): ConfigItem {
        return list[currentSelected]
    }


    data class ConfigItem(
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
}