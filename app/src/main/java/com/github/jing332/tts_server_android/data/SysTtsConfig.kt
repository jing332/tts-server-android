package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.VoiceProperty.Companion.DEFAULT_VOICE
import com.github.jing332.tts_server_android.data.VoiceProperty.Companion.DEFAULT_VOICE_ID
import com.github.jing332.tts_server_android.util.FileUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@kotlinx.serialization.Serializable
data class SysTtsConfig(
    var list: ArrayList<SysTtsConfigItem>,
    var isSplitSentences: Boolean = true,
    var isMultiVoice: Boolean = false,
    var isReplace: Boolean = false,
    var timeout: Int = 5000,
    var minDialogueLength: Int = 0
) {
    companion object {
        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts_config.json" }
        fun read(): SysTtsConfig {
            return try {
                val str = File(filepath).readText()
                App.jsonBuilder.decodeFromString<SysTtsConfig>(str)
            } catch (e: Exception) {
                e.printStackTrace()
                return SysTtsConfig()
            }
        }
    }

    constructor() : this(
        arrayListOf(
            SysTtsConfigItem(
                SysTtsUiData("晓晓 (zh-CN-XiaoxiaoNeural)", "无"), true,
                ReadAloudTarget.DEFAULT,
                VoiceProperty(DEFAULT_VOICE, DEFAULT_VOICE_ID)
            )
        ),
    )

    fun save() {
        FileUtils.saveFile(filepath, App.jsonBuilder.encodeToString(this).toByteArray())
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