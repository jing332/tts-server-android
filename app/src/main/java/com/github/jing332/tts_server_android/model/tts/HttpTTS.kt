package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsQuickEditView
import com.github.jing332.tts_server_android.util.setFadeAnim
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

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
) : Parcelable, BaseTTS() {
    override fun isRateFollowSystem(): Boolean {
        return VALUE_FOLLOW_SYSTEM == rate
    }

    override fun isPitchFollowSystem(): Boolean {
        return false
    }

    override fun getType(): String {
        return app.getString(R.string.custom)
    }

    override fun getDescription(): String {
        val rateStr =
            if (isRateFollowSystem()) app.getString(R.string.follow) else rate
        return "语速：<b>${rateStr}</b> | 音量：<b>${volume}</b>"
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        displayName: String,
        done: (modifiedData: BaseTTS?) -> Unit
    ) {
        val editView = HttpTtsQuickEditView(context).apply {
            setData(this@HttpTTS)
            setPadding(16)
        }

        AlertDialog.Builder(context).setView(editView)
            .setOnDismissListener {
                if (editView.rate == rate && editView.volume == volume)
                    done(null)
                else {
                    rate = editView.rate
                    volume = editView.volume
                    done(this)
                }
            }.setFadeAnim()
            .show()
    }

    @IgnoredOnParcel
    @kotlinx.serialization.Transient
    private lateinit var httpClient: OkHttpClient

    override fun onLoad() {
        httpClient = OkHttpClient.Builder()
            .connectTimeout(SysTtsConfig.requestTimeout.toLong(), TimeUnit.MILLISECONDS).build()

        runCatching { parseHeaders() }.onFailure { throw Throwable("解析请求头失败：$it") }
    }

    @IgnoredOnParcel
    private var httpHeaders: MutableMap<String, String> = mutableMapOf()

    private fun parseHeaders() {
        if (!header.isNullOrEmpty()) {
            httpHeaders = App.jsonBuilder.decodeFromString(header.toString())
        }
    }

    fun getAudioResponse(speakText: String): Response {
        val a =
            AnalyzeUrl(mUrl = url, speakText = speakText, speakSpeed = rate, speakVolume = volume)
        val urlOption = a.eval()
        return if (urlOption == null) { //GET
            Net.get(a.baseUrl) {
                if (!this@HttpTTS::httpClient.isInitialized) onLoad()
                okHttpClient = this@HttpTTS.httpClient
                setHeaders(httpHeaders.toHeaders())
            }.execute()
        } else {
            Net.post(a.baseUrl) {
                if (!this@HttpTTS::httpClient.isInitialized) onLoad()
                okHttpClient = this@HttpTTS.httpClient
                setHeaders(httpHeaders.toHeaders())
                body = urlOption.body.toString().toRequestBody(null)
            }.execute()
        }
    }

    override fun getAudio(speakText: String): ByteArray? {
        val resp = getAudioResponse(speakText)
        val body = resp.body?.bytes()
        if (resp.code != 200) throw  Throwable(body?.contentToString())

        resp.body?.close()
        return body
    }

    override fun getAudioStream(
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