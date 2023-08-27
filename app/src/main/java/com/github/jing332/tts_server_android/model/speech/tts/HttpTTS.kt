package com.github.jing332.tts_server_android.model.speech.tts

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.os.SystemClock
import com.drake.net.Net
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import okhttp3.Headers.Companion.toHeaders
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.InputStream

@Parcelize
@Serializable
@SerialName("http")
data class HttpTTS(
    var url: String = "",
    var header: String? = null,

    override var pitch: Int = 1,
    override var volume: Int = 1,
    override var rate: Int = 1,

    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: PlayerParams = PlayerParams(),
    override var audioParams: AudioParams = AudioParams(),
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),

    @Transient
    override var locale: String = "",

    ) : Parcelable, ITextToSpeechEngine() {
    override fun isRateFollowSystem(): Boolean {
        return VALUE_FOLLOW_SYSTEM == rate
    }

    override fun isPitchFollowSystem(): Boolean {
        return false
    }


    override fun getType(): String {
        return app.getString(R.string.custom)
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    @IgnoredOnParcel
    private var requestId: String = ""

    override fun onStop() {
        Net.cancelId(requestId)
    }

    override fun onLoad() {
        runCatching { parseHeaders() }.onFailure { throw Throwable("解析请求头失败：$it") }
    }

    @IgnoredOnParcel
    private var httpHeaders: MutableMap<String, String> = mutableMapOf()

    private fun parseHeaders() {
        if (!header.isNullOrEmpty()) {
            httpHeaders = AppConst.jsonBuilder.decodeFromString(header.toString())
        }
    }

    @Synchronized
    fun getAudioResponse(speakText: String): Response {
        requestId = "HTTP_TTS_${SystemClock.elapsedRealtime()}"
        val a =
            AnalyzeUrl(mUrl = url, speakText = speakText, speakSpeed = rate, speakVolume = volume)
        val urlOption = a.eval()
        return if (urlOption == null) { //GET
            Net.get(a.baseUrl) {
                setId(requestId)
                setHeaders(httpHeaders.toHeaders())
            }.execute()
        } else {
            Net.post(a.baseUrl) {
                setId(requestId)
                setHeaders(httpHeaders.toHeaders())
                body = urlOption.body.toString().toRequestBody(null)
            }.execute()
        }
    }

    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): InputStream? {
        val resp = getAudioResponse(speakText)
        return if (resp.isSuccessful) {
            resp.body?.byteStream()
        } else
            throw Exception("HTTP TTS 请求失败：${resp.code}, ${resp.message}")
    }
}