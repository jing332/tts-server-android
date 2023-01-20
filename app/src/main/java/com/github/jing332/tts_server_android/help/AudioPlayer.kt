package com.github.jing332.tts_server_android.help

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.drake.net.utils.runMain
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.help.ExoPlayerHelper.createMediaSourceFromByteArray
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class AudioPlayer(val context: Context, val scope: CoroutineScope = GlobalScope) {
    companion object {
        const val TAG = "AudioPlayer"
    }

    // APP内播放音频Job 用于 job.cancel() 取消播放
    private var mInAppPlayJob: Job? = null

    // APP内音频播放器 必须在主线程调用
    private val exoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                @SuppressLint("SwitchIntDef")
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            mInAppPlayJob?.cancel()
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }
            })
        }
    }

    /**
     * 播放音频 等待直到完毕
     */
    suspend fun play(audio: ByteArray, speed: Float = 1f, pitch: Float = 1f) {
        mInAppPlayJob = scope.launch {
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
        mInAppPlayJob?.join()
        mInAppPlayJob = null
    }

    fun stop() {
        mInAppPlayJob?.cancel()
        mInAppPlayJob = null
    }

    fun release() {
        stop()
        exoPlayer.release()
    }

}