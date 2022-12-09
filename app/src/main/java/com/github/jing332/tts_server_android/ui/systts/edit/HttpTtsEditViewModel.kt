package com.github.jing332.tts_server_android.ui.systts.edit

import android.media.MediaFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.help.ExoByteArrayMediaSource
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.service.systts.help.AudioDecoder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.launch

class HttpTtsEditViewModel : ViewModel() {
    private val exoPlayer = lazy {
        ExoPlayer.Builder(App.context).build().apply {
            playWhenReady = true
        }
    }

    // 创建音频媒体源
    private fun createMediaSourceFromByteArray(data: ByteArray): MediaSource {
        val factory = DataSource.Factory { ExoByteArrayMediaSource(data) }
        return DefaultMediaSourceFactory(App.context).setDataSourceFactory(factory)
            .createMediaSource(MediaItem.fromUri(""))
    }

    override fun onCleared() {
        super.onCleared()
        if (exoPlayer.isInitialized()) exoPlayer.value.release()
    }

    fun doTest(
        tts: HttpTTS,
        text: String,
        onSuccess: suspend (size: Int, sampleRate: Int, mime: String, contentType: String) -> Unit,
        onFailure: suspend (reason: String) -> Unit,
    ) {
        viewModelScope.launch {
            kotlin.runCatching {
                val resp = withIO { tts.getAudioResponse(text) }
                val data = withIO { resp.body?.bytes() }

                if (resp.code != 200) {
                    onFailure("服务器返回错误信息：\n${data?.decodeToString()}")
                    return@launch
                }

                if (data == null) onFailure("音频为空")
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
                onFailure(it.stackTraceToString())
            }
        }
    }

    fun stopPlay() {
        exoPlayer.value.stop()
    }
}