package com.github.jing332.tts_server_android.help.audio.exo

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.source.MediaSource
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.help.audio.AudioDecoderException
import com.github.jing332.tts_server_android.help.audio.ExoPlayerHelper
import kotlinx.coroutines.*
import java.io.InputStream
import java.nio.ByteBuffer

@SuppressLint("UnsafeOptInUsageError")
class ExoAudioDecoder(val context: Context) {
    companion object {
        private const val CANCEL_MESSAGE_ENDED = "CANCEL_MESSAGE_ENDED"
        private const val CANCEL_MESSAGE_ERROR = "CANCEL_MESSAGE_ERROR"
    }

    private var mWaitJob: Job? = null
    var callback: Callback? = null

    private val exoPlayer by lazy {
        val rendererFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DecoderAudioSink { callback?.onReadPcmAudio(it) }
            }
        }

        ExoPlayer.Builder(context, rendererFactory).build().apply {
            addListener(object : Player.Listener {
                @SuppressLint("SwitchIntDef")
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            mWaitJob?.cancel(CANCEL_MESSAGE_ENDED)
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    mWaitJob?.cancel(CANCEL_MESSAGE_ERROR, error)
                }
            })

            playWhenReady = true
        }
    }

    suspend fun doDecode(bytes: ByteArray) {
        decodeInternal(ExoPlayerHelper.createMediaSourceFromByteArray(bytes))
    }

    suspend fun doDecode(inputStream: InputStream) {
        decodeInternal(ExoPlayerHelper.createMediaSourceFromInputStream(inputStream))
    }

    private suspend fun decodeInternal(mediaSource: MediaSource) {
        withMain {
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
        }

        var throwable: Throwable? = null
        coroutineScope {
            mWaitJob = launch {
                try {
                    awaitCancellation()
                } catch (e: CancellationException) {
                    if (e.message == CANCEL_MESSAGE_ERROR) {
                        throwable = e.cause
                        exoPlayer.stop()
                    }
                }
            }
        }
        mWaitJob?.join()
        mWaitJob = null

        throwable?.let {
            throw AudioDecoderException(
                message = "ExoPlayer解码失败：${it.message}",
                cause = it
            )
        }
    }


    fun interface Callback {
        fun onReadPcmAudio(byteBuffer: ByteBuffer)
    }

}