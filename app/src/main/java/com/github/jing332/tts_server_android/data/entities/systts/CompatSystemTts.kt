package com.github.jing332.tts_server_android.data.entities.systts

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import kotlinx.serialization.Serializable


@Serializable
data class CompatSystemTts(
    @kotlinx.serialization.Transient
    val id: Long = 0,

    // 是否启用
    @kotlinx.serialization.Transient
    var isEnabled: Boolean = false,

    // UI显示名称
    var displayName: String = "",

    // 朗读目标
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.ALL,

    // TTS属性
    var tts: ITextToSpeechEngine,
) {
}
