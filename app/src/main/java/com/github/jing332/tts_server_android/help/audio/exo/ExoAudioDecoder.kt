package com.github.jing332.tts_server_android.help.audio.exo

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.audio.TeeAudioProcessor
import kotlinx.coroutines.*
import java.io.InputStream
import java.nio.ByteBuffer

class ExoAudioDecoder(val context: Context) {
    companion object {
        private const val WAIT_JOB_CANCEL_MSG = "STATE_ENDED"
    }

    private var mWaitJob: Job? = null
    var callback: Callback? = null

    private val exoPlayer by lazy {
        val rendererFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
                enableOffload: Boolean
            ): AudioSink {
                return DecoderAudioSink { callback?.onReadPcmAudio(it) }
            }
        }

        ExoPlayer.Builder(context, rendererFactory).build().apply {
            addListener(object : Player.Listener {
                @SuppressLint("SwitchIntDef")
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            mWaitJob?.cancel(WAIT_JOB_CANCEL_MSG)
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    mWaitJob?.cancel(WAIT_JOB_CANCEL_MSG)
                }
            })

            playWhenReady = true
        }
    }


    suspend fun doDecode(bytes: ByteArray) {
        withMain {
            exoPlayer.setMediaSource(ExoPlayerHelper.createMediaSourceFromByteArray(bytes))
            exoPlayer.prepare()
        }

        coroutineScope {
            mWaitJob = launch {
                try {
                    awaitCancellation()
                } catch (e: CancellationException) { // doDecode 挂起函数被取消
                    if (e.message != WAIT_JOB_CANCEL_MSG) exoPlayer.stop()
                }
            }
        }
        mWaitJob?.join()
    }

    suspend fun doDecode(inputStream: InputStream) {
        withMain {
            exoPlayer.setMediaSource(ExoPlayerHelper.createMediaSourceFromInputStream(inputStream))
            exoPlayer.prepare()
        }

        coroutineScope {
            mWaitJob = launch {
                try {
                    awaitCancellation()
                } catch (e: CancellationException) { // doDecode 挂起函数被取消
                    if (e.message != WAIT_JOB_CANCEL_MSG) exoPlayer.stop()
                }
            }
        }
        mWaitJob?.join()
    }


    fun interface Callback {
        fun onReadPcmAudio(byteBuffer: ByteBuffer)
    }

}