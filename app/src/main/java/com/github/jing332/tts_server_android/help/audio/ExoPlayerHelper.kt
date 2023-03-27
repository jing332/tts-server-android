package com.github.jing332.tts_server_android.help.audio

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.app
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.analytics.DefaultPlaybackSessionManager
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource

object ExoPlayerHelper {

    // 创建音频媒体源
    fun createMediaSourceFromByteArray(data: ByteArray): MediaSource {
        val factory = DataSource.Factory { ExoByteArrayMediaSource(data) }
        return DefaultMediaSourceFactory(App.context).setDataSourceFactory(factory)
            .createMediaSource(MediaItem.fromUri(""))
    }
}