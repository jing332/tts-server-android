package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
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

    abstract var pitch: Int
    abstract var volume: Int
    abstract var rate: Int
    abstract var audioFormat: BaseAudioFormat
    abstract var audioPlayer: AudioPlayer

    /**
     * 语速是否跟随系统
     */
    open fun isRateFollowSystem(): Boolean = rate == VALUE_FOLLOW_SYSTEM

    /**
     * 音高是否跟随系统
     */
    open fun isPitchFollowSystem(): Boolean = rate == VALUE_FOLLOW_SYSTEM

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
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    )


    open fun onLoad() {}

    open fun onStop() {}

    open fun onDestroy() {}

    /**
     * 是否为 直接播放
     */
    open fun isDirectPlay(): Boolean = false

    /**
     * 直接播放并等待完毕
     */
    open fun directPlay(text: String): Boolean = false

    /**
     * 完整获取音频
     */
    open fun getAudio(speakText: String): ByteArray? = null

    /**
     * 获取PCM音频流
     * @return 是否从上次断点处开始
     */
    open fun getAudioStream(
        speakText: String,
        chunkSize: Int = 0,
        onData: (ByteArray?) -> Unit
    ) {
    }
}