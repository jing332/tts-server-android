package com.github.jing332.tts_server_android.ui.systts.edit

import android.media.MediaFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.help.ExoPlayerHelper.createMediaSourceFromByteArray
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.service.systts.help.AudioDecoder
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.launch

class HttpTtsEditViewModel : ViewModel() {
    private val exoPlayer = lazy {
        ExoPlayer.Builder(App.context).build().apply {
            playWhenReady = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (exoPlayer.isInitialized()) exoPlayer.value.release()
    }

    fun doTest(
        tts: HttpTTS,
        text: String,
        onSuccess: suspend (size: Int, sampleRate: Int, mime: String, contentType: String) -> Unit,
        onFailure: suspend (reason: Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            kotlin.runCatching {
                val resp = withIO { tts.getAudioResponse(text) }
                val data = withIO { resp.body?.bytes() }

                if (resp.code != 200) {
                    onFailure.invoke(Exception("服务器返回错误信息：\n${data?.decodeToString()}"))
                    return@launch
                }

                if (data == null) onFailure.invoke(Exception("音频为空"))
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

                    onSuccess(it.size, mSampleRate, mMime, contentType)
                    exoPlayer.value.apply {
                        setMediaSource(createMediaSourceFromByteArray(it))
                        prepare()
                    }

                }
            }.onFailure {
                onFailure.invoke(it)
            }
        }
    }

    fun stopPlay() {
        exoPlayer.value.stop()
    }
}