package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.ui.custom.HttpTtsQuickEditView
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
        done: (modifiedData: BaseTTS?) -> Unit
    ) {
        val editView = HttpTtsQuickEditView(context)
        editView.rate = rate
        editView.volume = volume
        editView.callBack = object : HttpTtsQuickEditView.CallBack {
            override fun onValueChanged(rate: Int, volume: Int): String {
                kotlin.runCatching {
                    val result = AnalyzeUrl(
                        mUrl = url,
                        speakText = "",
                        speakSpeed = rate,
                        speakVolume = volume
                    ).eval()
                    return result.body ?: "解析url失败"
                }.onFailure {
                    return "${it.message}"
                }

                return ""
            }
        }


        val dialog = AlertDialog.Builder(context).setView(editView)
            .setOnDismissListener {
                if (editView.rate == rate && editView.volume == volume)
                    done(null)
                else {
                    rate = editView.rate
                    volume = editView.volume
                    done(this)
                }
            }.create()
        dialog.window?.setWindowAnimations(R.style.dialogFadeStyle)
        dialog.show()
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
        urlOption.let {
            return Net.post(a.baseUrl) {
                if (!this@HttpTTS::httpClient.isInitialized) onLoad()
                okHttpClient = this@HttpTTS.httpClient
                setHeaders(httpHeaders.toHeaders())
                body = it.body.toString().toRequestBody(null)
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