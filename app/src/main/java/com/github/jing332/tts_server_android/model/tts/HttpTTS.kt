package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.drake.net.Net
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.ui.custom.HttpTtsNumericalEditView
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
) : Parcelable, BaseTTS() {

    override fun getType(): String {
        return "自定义"
    }

    override fun getDescription(): String {
        return "语速：<b>${rate}</b> | 音量：<b>${volume}</b>"
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        done: (modifiedData: BaseTTS?) -> Unit
    ) {
        val editView = HttpTtsNumericalEditView(context)
        editView.rate = rate
        editView.volume = volume
        editView.callBack = object : HttpTtsNumericalEditView.CallBack {
            override fun onValueChanged(rate: Int, volume: Int): String {
                this@HttpTTS.rate = rate
                this@HttpTTS.volume = volume
                kotlin.runCatching {
                    val result = AnalyzeUrl(mUrl = url, speakText = "", speakSpeed = rate).eval()
                    return result?.body ?: "内容为空"
                }.onFailure {
                    return "${it.message}"
                }

                return ""
            }
        }

        AlertDialog.Builder(context).setTitle("数值调节").setView(editView)
            .setOnDismissListener {
                done(this)
            }
            .show()
    }

    override fun getAudio(speakText: String): ByteArray? {
        val a = AnalyzeUrl(mUrl = url, speakText = speakText, speakSpeed = rate)
        val urlOption = a.eval()
        urlOption?.let {
            val resp = Net.post(a.baseUrl) {
                body = it.body.toString().toRequestBody(null)
            }.execute<Response>()
            val body = resp.body?.bytes()
            if (resp.code != 200) throw  Throwable(body?.contentToString())

            resp.body?.close()
            return body
        }
        throw Throwable("url格式错误")
    }

    override fun getAudioStream(
        speakText: String,
        chunkSize: Int,
        onData: (ByteArray?) -> Unit
    ): Boolean {
        onData(getAudio(speakText))
        return false

        /*kotlin.runCatching {
            val a = AnalyzeUrl(mUrl = url, speakText = speakText, speakSpeed = rate)
            val urlOption = a.eval()
            urlOption?.let {
                val resp = Net.post(a.baseUrl) {
                    body = it.body.toString().toRequestBody(null)
                }.execute<Response>()
                val data = ByteArray(chunkSize)

                resp.body?.byteStream()?.let {
                    while (true) {
                        val index = it.read(data)
                        if (index == 0) continue
                        if (index == -1) break

                        onData(data.copyOfRange(0, index))
                    }
                }
                resp.body?.close()
            }
        }.onFailure {
            return it.message
        }*/
    }
}