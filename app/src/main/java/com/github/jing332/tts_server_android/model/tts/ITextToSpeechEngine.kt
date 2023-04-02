package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.View
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.util.toHtmlBold
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Parcelize
@Serializable
@JsonClassDiscriminator("#type")
sealed class ITextToSpeechEngine(
    @Transient
    @IgnoredOnParcel
    protected val context: Context = app
) : Parcelable {
    companion object {
        const val VALUE_FOLLOW_SYSTEM = 0
    }

    abstract var pitch: Int
    abstract var volume: Int
    abstract var rate: Int

    abstract var info: TtsInfo
    abstract var audioFormat: BaseAudioFormat
    abstract var audioPlayer: PlayerParams

    /**
     * 语速是否跟随系统
     */
    open fun isRateFollowSystem(): Boolean = rate == VALUE_FOLLOW_SYSTEM

    /**
     * 音高是否跟随系统
     */
    open fun isPitchFollowSystem(): Boolean = pitch == VALUE_FOLLOW_SYSTEM


    abstract fun getEditActivity(): Class<out Activity>

    /**
     * UI 右下角类型
     */
    abstract fun getType(): String

    /**
     * UI 底部的格式
     */
    open fun getBottomContent(): String {
        return audioFormat.toString()
    }

    /**
     * UI 显示名称下方的描述，如音量语速等
     */
    open fun getDescription(): String {
        val followStr = context.getString(R.string.follow).toHtmlBold()
        return context.getString(
            R.string.systts_play_params_description,
            if (isRateFollowSystem()) followStr else "$rate".toHtmlBold(),
            "$volume".toHtmlBold(),
            if (isPitchFollowSystem()) followStr else "$pitch".toHtmlBold()
        )
    }

    /**
     * UI 当点击 描述TextView
     */
    abstract fun onDescriptionClick(
        activity: Activity,
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
     * 开始播放 用于本地TTS的直接播放
     */
    open suspend fun startPlay(text: String, sysRate: Int = 0, sysPitch: Int = 0): Boolean = false

    /**
     * 完整获取音频
     */
    open suspend fun getAudio(speakText: String, sysRate: Int = 0, sysPitch: Int = 0): ByteArray? =
        null

    fun rateWithFollow(sysRate: Int): Int {
        return if (isRateFollowSystem()) sysRate else rate
    }

    fun pitchWithFollow(sysPitch: Int): Int {
        return if (isPitchFollowSystem()) sysPitch else pitch
    }

    /**
     * 获取PCM音频流
     */
    open suspend fun getAudioStream(
        speakText: String,
        chunkSize: Int = 0,
        onData: (ByteArray?) -> Unit
    ) {
    }
}