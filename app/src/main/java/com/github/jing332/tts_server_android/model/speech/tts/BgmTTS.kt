package com.github.jing332.tts_server_android.model.speech.tts

import android.app.Activity
import android.content.Context
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.utils.toHtmlBold
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.InputStream

@Parcelize
@Serializable
@SerialName("bgm")
data class BgmTTS(
    var musicList: MutableSet<String> = mutableSetOf(),

    override var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: PlayerParams = PlayerParams(),

    @Transient
    @IgnoredOnParcel
    override var audioParams: AudioParams = AudioParams(),
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),

    override var locale: String = ""
) : ITextToSpeechEngine() {
    override fun getType() = "BGM"

    override fun getDescription(): String {
        val volStr = if (volume == 0) context.getString(R.string.follow) else volume.toString()
        return context.getString(R.string.systts_bgm_description, volStr.toHtmlBold())
    }

    override fun getBottomContent(): String =
        context.getString(R.string.total_n_folders, musicList.size.toString())

    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): InputStream? {
        throw Exception("请在编辑界面中点击音乐路径进行测试播放")
    }
}