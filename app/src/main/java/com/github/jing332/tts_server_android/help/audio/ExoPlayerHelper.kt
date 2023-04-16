package com.github.jing332.tts_server_android.help.audio

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.help.audio.exo.InputStreamDataSource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import java.io.InputStream


object ExoPlayerHelper {
    fun createMediaSourceFromInputStream(inputStream: InputStream): MediaSource {
        val factory = DataSource.Factory {
            InputStreamDataSource(inputStream)
        }
        return DefaultMediaSourceFactory(App.context).setDataSourceFactory(factory)
            .createMediaSource(MediaItem.fromUri(""))
    }

    // 创建音频媒体源
    fun createMediaSourceFromByteArray(data: ByteArray): MediaSource {
        val factory = DataSource.Factory { ByteArrayDataSource(data) }
        return DefaultMediaSourceFactory(App.context).setDataSourceFactory(factory)
            .createMediaSource(MediaItem.fromUri(""))
    }
}