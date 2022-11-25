package com.github.jing332.tts_server_android.ui.systts.edit

import android.media.MediaFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.service.systts.help.AudioDecoder
import com.github.jing332.tts_server_android.util.runOnIO

class HttpTtsEditViewModel : ViewModel() {
    fun doTest(
        url: String,
        testText: String,
        onSuccess: suspend (size: Int, sampleRate: Int, mime: String) -> Unit,
        onFailure: suspend (reason: String) -> Unit,
    ) {
        viewModelScope.runOnIO {
            kotlin.runCatching {
                val audio = HttpTTS(url).getAudio(testText)
                if (audio == null) withMain { onFailure("音频为空") }

                audio?.let {
                    val ad = AudioDecoder()
                    val formats = ad.getFormats(it)

                    var mSampleRate = 0
                    var mMime = "无"
                    if (formats.isNotEmpty()) {
                        mSampleRate = formats[0].getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        mMime = formats[0].getString(MediaFormat.KEY_MIME) ?: ""
                    }

                    withMain { onSuccess(it.size, mSampleRate, mMime) }
                }
            }.onFailure {
                withMain { onFailure(it.message.toString()) }
            }
        }
    }

    fun toSampleRateIndex(sampleRate: Int): Int {
        return when (sampleRate) {
            24000 -> 1
            48000 -> 2
            else -> 0 // 16000
        }
    }
}