package com.github.jing332.tts_server_android.speech

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.drake.net.Net
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.util.runOnIO
import com.github.jing332.tts_server_android.util.toast
import okhttp3.Response
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AudioDecodeTest {
    @Test
    fun audioStream() {
        val SAMPLE_RATE = 44100; // 采样率
        val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO; // 单声道
        val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16bit 采样精度

        // 初始化音频播放器

        // 初始化音频播放器
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,  // 音频流类型
            SAMPLE_RATE,  // 采样率
            CHANNEL_CONFIG,  // 声道配置
            AUDIO_FORMAT,  // 采样精度
            AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT),  // 缓冲区大小
            AudioTrack.MODE_STREAM // 播放模式（流模式）
        )
        audioTrack.play()

        val resp =
            Net.get("http://m801.music.126.net/20230409165255/4e2fcbce72edb67814032c7625c9b32f/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/7655605515/481c/ed22/693f/e3f82a1245ec63c2adfef44da56f1904.mp3")
                .execute<Response>()
        if (resp.isSuccessful) {
            val input = resp.body!!.byteStream()
            AudioDecoder().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decode(input, 44100) {
                        println(it.size)
                        audioTrack.write(it, 0, it.size)
                    }
                }
            }

        } else {
//                toast("fail & ${resp.code}")
        }
    }
}