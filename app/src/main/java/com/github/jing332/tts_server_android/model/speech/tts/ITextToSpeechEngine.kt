package com.github.jing332.tts_server_android.model.speech.tts

import android.content.Context
import android.os.Parcelable
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.utils.toHtmlBold
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import java.io.ByteArrayInputStream
import java.io.InputStream

@OptIn(ExperimentalSerializationApi::class)
@Parcelize
@Serializable
@JsonClassDiscriminator("#type")
sealed class ITextToSpeechEngine(
    @Transient
    @IgnoredOnParcel
    var context: Context = app,

    ) : Parcelable {
    companion object {
        const val VALUE_FOLLOW_SYSTEM = 0
    }

    abstract var pitch: Int
    abstract var volume: Int
    abstract val rate: Int

    abstract var locale: String

    abstract var speechRule: SpeechRuleInfo
    abstract var audioFormat: BaseAudioFormat
    abstract var audioPlayer: PlayerParams
    abstract var audioParams: AudioParams


    /**
     * 语速是否跟随系统
     */
    open fun isRateFollowSystem(): Boolean = rate == VALUE_FOLLOW_SYSTEM

    /**
     * 音高是否跟随系统
     */
    open fun isPitchFollowSystem(): Boolean = pitch == VALUE_FOLLOW_SYSTEM

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

    open fun onLoad() {}

    open fun onStop() {}

    open fun onDestroy() {}

    /**
     * 是否为 直接播放
     */
    open fun isDirectPlay(): Boolean = false

    protected open suspend fun startPlay(text: String, rate: Int = 0, pitch: Int = 0): Boolean =
        false

    /**
     * 播放音频 参数自动判断是否随系统
     */
    open suspend fun startPlayWithSystemParams(
        text: String,
        sysRate: Int = 0,
        sysPitch: Int = 0
    ): Boolean {
        val r = if (isRateFollowSystem()) sysRate else this.rate
        val p = if (isPitchFollowSystem()) sysPitch else this.pitch
        return startPlay(text, r, p)
    }

    open suspend fun getAudio(
        speakText: String,
        rate: Int = 50,
        pitch: Int = 0
    ): InputStream? = null

    suspend fun getAudioWithSystemParams(
        text: String,
        sysRate: Int = 50,
        sysPitch: Int = 0
    ): InputStream? {

        val r = if (isRateFollowSystem()) sysRate else this.rate
        val p = if (isPitchFollowSystem()) sysPitch else this.pitch
        return getAudio(text, r, p)
    }

//    open suspend fun getAudioStream(text: String, rate: Int, pitch: Int): InputStream? = null
//
//    suspend fun getAudioStreamSysParams(
//        text: String,
//        sysRate: Int = 50,
//        sysPitch: Int = 0
//    ): InputStream? {
//        val r = if (isRateFollowSystem()) sysRate else this.rate
//        val p = if (isPitchFollowSystem()) sysPitch else this.pitch
//        return getAudioStream(text, r, p)
//    }

    /**
     * 获取PCM音频流
     */
    @Deprecated("暂时不用")
    open suspend fun getAudioStream(
        speakText: String,
        chunkSize: Int = 0,
        onData: (ByteArray?) -> Unit
    ) {
    }


    protected fun ByteArray?.toStream(): ByteArrayInputStream? {
        return if (this == null) null else ByteArrayInputStream(this)
    }
}