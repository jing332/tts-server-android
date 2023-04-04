package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsHttpEditBottomSheetBinding
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.ui.systts.edit.http.HttpTtsEditActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import okhttp3.Headers.Companion.toHeaders
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

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
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),

    ) : Parcelable, ITextToSpeechEngine() {
    override fun isRateFollowSystem(): Boolean {
        return VALUE_FOLLOW_SYSTEM == rate
    }

    override fun isPitchFollowSystem(): Boolean {
        return false
    }

    override fun getEditActivity(): Class<out Activity> = HttpTtsEditActivity::class.java

    override fun getType(): String {
        return app.getString(R.string.custom)
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    @Suppress("DEPRECATION")
    override fun onDescriptionClick(
        activity: Activity,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val binding =
            SysttsHttpEditBottomSheetBinding.inflate(activity.layoutInflater, null, false)
        binding.apply {
            basicEdit.setData(data)
            editView.setData(this@HttpTTS)
            root.minimumHeight = activity.windowManager.defaultDisplay.height
        }

        BottomSheetDialog(activity).apply {
            setContentView(binding.root)
            setOnDismissListener { done(data) }
            show()
        }
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
            httpHeaders = App.jsonBuilder.decodeFromString(header.toString())
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

    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): ByteArray? {
        val resp = getAudioResponse(speakText)
        val body = resp.body?.bytes()
        if (resp.code != 200) throw Throwable("${resp.message}, ${body.contentToString()}")

        resp.body?.close()
        return body
    }

    override suspend fun getAudioStream(
        speakText: String,
        chunkSize: Int,
        onData: (ByteArray?) -> Unit
    ) {
        onData(getAudio(speakText))
        /* getAudioResponse(speakText).body?.byteStream()?.let {
             val data = ByteArray(chunkSize)
             while (true) {
                 val index = it.read(data)
                 if (index == 0) continue
                 if (index == -1) break

                 onData(data.copyOfRange(0, index))
             }*/
    }
}