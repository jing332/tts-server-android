package com.github.jing332.tts_server_android.help.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.annotation.FloatRange
import com.drake.net.utils.runMain
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper.createMediaSourceFromByteArray
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper.createMediaSourceFromInputStream
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.*
import com.google.android.exoplayer2.source.MediaSource
import kotlinx.coroutines.*
import java.io.InputStream
import java.nio.ByteBuffer

class AudioPlayer(val context: Context) {
    companion object {
        const val TAG = "AudioPlayer"

        const val MSG_STATE_ENDED = "MSG_STATE_ENDED"
        const val MSG_PLAYER_ERROR = "MSG_PLAYER_ERROR"
    }

    // APP内播放音频Job 用于 job.cancel() 取消播放
    private var mPlayWaitJob: Job? = null

    // APP内音频播放器 必须在主线程调用
    private val exoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                @SuppressLint("SwitchIntDef")
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            mPlayWaitJob?.cancel(MSG_STATE_ENDED)
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    mPlayWaitJob?.cancel("onPlayerError", error)
                }
            })
        }
    }

    suspend fun play(audio: InputStream, speed: Float = 1f, volume: Float = 1f, pitch: Float = 1f) {
        playInternal(createMediaSourceFromInputStream(audio), speed, volume, pitch)
    }

    suspend fun play(audio: ByteArray, speed: Float = 1f, volume: Float = 1f, pitch: Float = 1f) {
        playInternal(createMediaSourceFromByteArray(audio), speed, volume, pitch)
    }

    private suspend fun playInternal(
        mediaSource: MediaSource,
        speed: Float = 1f,
        @FloatRange(from = 0.0, to = 1.0) volume: Float = 1f,
        pitch: Float = 1f,
    ) = coroutineScope {
        var throwable: Throwable? = null
        mPlayWaitJob = launch() {
            try {
                withMain {
                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.playbackParameters =
                        PlaybackParameters(speed, pitch)
                    exoPlayer.volume = volume
                    exoPlayer.prepare()
                }
                // 一直等待 直到 job.cancel
                awaitCancellation()
            } catch (e: CancellationException) {
                if (e.message == MSG_STATE_ENDED) {
                    runMain { exoPlayer.stop() }
                } else if (e.message == MSG_PLAYER_ERROR) {
                    throwable = e.cause
                } else {
                    runMain { exoPlayer.stop() }
                }
            }
        }
        mPlayWaitJob?.join()
        mPlayWaitJob = null

        throwable?.let { throw it }
    }

    fun stop() {
        mPlayWaitJob?.cancel()
        mPlayWaitJob = null
    }

    fun release() {
        stop()
        exoPlayer.release()
    }

}