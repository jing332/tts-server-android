package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.util.FileUtils
import java.io.Serializable

data class SysTtsConfig(
    var list: ArrayList<SysTtsConfigItem>,
    var currentSelected: Int, //默认 全局
    var currentAside: Int, //旁白
    var currentDialogue: Int, //对话
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
                TtsApiType.CREATION,
                "zh-CN",
                "zh-CN-XiaoxiaoNeural",
                "", 100, "",
                "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
                "audio-24khz-48kbitrate-mono-mp3",
                50, 0
            )
        ), 0, -1, -1, true, false
    )

    fun save() {
        FileUtils.saveObject(this, filepath)
    }

    fun selectedItem(): SysTtsConfigItem? {
        return list.getOrNull(currentSelected)
    }

    fun currentAsideItem(): SysTtsConfigItem? {
        return list.getOrNull(currentAside)
    }

    fun currentDialogueItem(): SysTtsConfigItem? {
        return list.getOrNull(currentDialogue)
    }
}