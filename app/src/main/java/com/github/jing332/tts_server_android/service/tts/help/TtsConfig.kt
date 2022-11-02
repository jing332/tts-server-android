package com.github.jing332.tts_server_android.service.tts.help

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.util.FileUtils
import java.io.Serializable

data class TtsConfig(
    var list: List<ConfigItem>,
    var currentSelected: Int,
    var currentSelectedDialogue: Int,
    var isSplitSentences: Boolean,
    var isMultiVoice: Boolean
) : Serializable {
    data class ConfigItem(
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
    }

    companion object {
        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts.dat" }

        fun read(): TtsConfig {
            return FileUtils.readObject<TtsConfig>(filepath) ?: TtsConfig()
        }
    }

    fun save() {
        FileUtils.saveObject(this, filepath)
    }

    constructor() : this(
        listOf(
            ConfigItem(
                TtsApiType.CREATION,
                "zh-CN",
                "zh-CN-XiaoxiaoNeural",
                "", 100, "",
                "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
                "audio-24khz-48kbitrate-mono-mp3",
                50, 0
            )
        ), 0, -1, false, false
    )

}