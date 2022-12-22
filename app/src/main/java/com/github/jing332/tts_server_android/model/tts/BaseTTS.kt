package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Parcelize
@Serializable
@JsonClassDiscriminator("#type")
sealed class BaseTTS : Parcelable {
    companion object {
        const val VALUE_FOLLOW_SYSTEM = 0
    }

    /**
     * 设置语速和音调 自动判断是否跟随系统
     */
    fun setPlayBackParameters(rate: Int, pitch: Int): BaseTTS {
        if (isRateFollowSystem()) this.rate = rate
        if (isPitchFollowSystem()) this.pitch = pitch
        return this
    }

    abstract var audioFormat: BaseAudioFormat
    abstract var pitch: Int
    abstract var volume: Int
    abstract var rate: Int

    /**
     * 语速是否跟随系统
     */
    abstract fun isRateFollowSystem(): Boolean

    /**
     * 音高是否跟随系统
     */
    abstract fun isPitchFollowSystem(): Boolean

    /**
     * UI 右下角类型
     */
    abstract fun getType(): String

    /**
     * UI 底部的格式
     */
    abstract fun getBottomContent(): String

    /**
     * UI 显示名称下方的描述，如音量语速等
     */
    abstract fun getDescription(): String

    /**
     * UI 当点击 描述TextView
     */
    abstract fun onDescriptionClick(
        context: Context,
        view: View?,
        displayName: String,
        done: (modifiedData: BaseTTS?) -> Unit
    )

    /**
     * 加载配置时调用
     */
    abstract fun onLoad()

    /**
     * 完整获取音频
     */
    abstract fun getAudio(speakText: String): ByteArray?

    /**
     * 获取PCM音频流
     * @return 是否从上次断点处开始
     */
    abstract fun getAudioStream(
        speakText: String,
        chunkSize: Int = 0,
        onData: (ByteArray?) -> Unit
    )
}