package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.media.MediaFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.service.systts.help.AudioDecoder
import com.github.jing332.tts_server_android.util.StringUtils.getExceptionMessageChain
import com.github.jing332.tts_server_android.util.runOnIO

class HttpTtsEditViewModel : ViewModel() {
    fun doTest(
        url: String,
        testText: String,
        headers: String,
        onSuccess: suspend (size: Int, sampleRate: Int, mime: String, contentType: String) -> Unit,
        onFailure: suspend (reason: String) -> Unit,
    ) {
        viewModelScope.runOnIO {
            kotlin.runCatching {
                val tts = HttpTTS(url, headers).apply { onLoad() }
                val resp = tts.getAudioResponse(testText)
                val data = resp.body?.bytes()

                if (resp.code != 200) {
                    withMain { onFailure("服务器返回错误信息：${data?.decodeToString()}") }
                    return@runOnIO
                }

                if (data == null) withMain { onFailure("音频为空") }
                val contentType = resp.header("Content-Type", "无") ?: "无"

                data?.let {
                    val ad = AudioDecoder()
                    val formats = ad.getFormats(it)
                    resp.body?.close()

                    var mSampleRate = 0
                    var mMime = "无"
                    if (formats.isNotEmpty()) {
                        mSampleRate = formats[0].getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        mMime = formats[0].getString(MediaFormat.KEY_MIME) ?: ""
                    }

                    withMain { onSuccess(it.size, mSampleRate, mMime, contentType) }
                }
            }.onFailure {
                withMain { onFailure(getExceptionMessageChain(it).toString()) }
            }
        }
    }

    fun toSampleRateIndex(sampleRate: Int, context: Context): Int {
        context.resources.getStringArray(R.array.sample_rate_list).forEachIndexed { index, s ->
            if (sampleRate == s.toInt())
                return index
        }
        return 0
    }
}