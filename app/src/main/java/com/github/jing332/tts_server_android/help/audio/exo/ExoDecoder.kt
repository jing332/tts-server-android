package com.github.jing332.tts_server_android.help.audio.exo

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.help.audio.ExoByteArrayMediaSource
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import java.io.InputStream

class ExoDecoder {
    val exoPlayer by lazy {
        ExoPlayer.Builder(app).build().apply {
            playWhenReady = true
        }
    }

    fun play(inputStream: InputStream) {
        exoPlayer.setMediaSource(ExoPlayerHelper.createMediaSourceFromInputStream(inputStream))
        exoPlayer.play()
    }

}