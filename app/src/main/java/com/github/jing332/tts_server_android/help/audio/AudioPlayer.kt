package com.github.jing332.tts_server_android.help.audio

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.drake.net.utils.runMain
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper.createMediaSourceFromByteArray
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper.createMediaSourceFromInputStream
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import java.io.InputStream

class AudioPlayer(val context: Context) {
    companion object {
        const val TAG = "AudioPlayer"
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
                            mPlayWaitJob?.cancel()
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }
            })
        }
    }

    suspend fun play(audio: InputStream, speed: Float = 1f, pitch: Float = 1f) = coroutineScope {
        mPlayWaitJob = launch {
            try {
                withMain {
                    exoPlayer.setMediaSource(createMediaSourceFromInputStream(audio))
                    exoPlayer.playbackParameters =
                        PlaybackParameters(speed, pitch)
                    exoPlayer.prepare()
                }
                // 一直等待 直到 job.cancel
                awaitCancellation()
            } catch (e: CancellationException) {
                Log.w(TAG, "in-app play job cancel: ${e.message}")
                runMain { exoPlayer.stop() }
            }
        }
        mPlayWaitJob?.join()
        mPlayWaitJob = null
    }

    /**
     * 播放音频 等待直到完毕
     */
    suspend fun play(audio: ByteArray, speed: Float = 1f, pitch: Float = 1f) = coroutineScope {
        mPlayWaitJob = launch {
            try {
                withMain {
                    exoPlayer.setMediaSource(createMediaSourceFromByteArray(audio))
                    exoPlayer.playbackParameters =
                        PlaybackParameters(speed, pitch)
                    exoPlayer.prepare()
                }
                // 一直等待 直到 job.cancel
                awaitCancellation()
            } catch (e: CancellationException) {
                Log.w(TAG, "in-app play job cancel: ${e.message}")
                runMain { exoPlayer.stop() }
            }
        }
        mPlayWaitJob?.join()
        mPlayWaitJob = null
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