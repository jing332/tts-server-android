package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.VoiceProperty.Companion.DEFAULT_VOICE
import com.github.jing332.tts_server_android.data.VoiceProperty.Companion.DEFAULT_VOICE_ID
import com.github.jing332.tts_server_android.service.systts.help.TtsAudioFormat
import com.github.jing332.tts_server_android.util.FileUtils
import java.io.Serializable

data class SysTtsConfig(
    var list: ArrayList<SysTtsConfigItem>,
    var isSplitSentences: Boolean,
    var isMultiVoice: Boolean
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts.dat" }
        fun read(): SysTtsConfig {
            val data = FileUtils.readObject<SysTtsConfig>(filepath)
            data?.let {
                if (it.list.isNotEmpty())
                    return it
            }
            return SysTtsConfig()
        }
    }

    constructor() : this(
        arrayListOf(
            SysTtsConfigItem(
                TtsConfigListItemData("晓晓 (zh-CN-XiaoxiaoNeural)", "无"), true,
                ReadAloudTarget.DEFAULT,
                "zh-CN",
                VoiceProperty(DEFAULT_VOICE, DEFAULT_VOICE_ID),
                TtsAudioFormat.DEFAULT,
            )
        ),  true, false
    )

    fun save() {
        FileUtils.saveObject(this, filepath)
    }

    fun selectedItem(): SysTtsConfigItem? {
        list.forEach {
            if (it.isEnabled && it.readAloudTarget == ReadAloudTarget.DEFAULT)
                return it
        }
        return null
    }

    fun currentAsideItem(): SysTtsConfigItem? {
        list.forEach {
            if (it.isEnabled && it.readAloudTarget == ReadAloudTarget.ASIDE)
                return it
        }
        return null
    }

    fun currentDialogueItem(): SysTtsConfigItem? {
        list.forEach {
            if (it.isEnabled && it.readAloudTarget == ReadAloudTarget.DIALOGUE)
                return it
        }
        return null
    }
}