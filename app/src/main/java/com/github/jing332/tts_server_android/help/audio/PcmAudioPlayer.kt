package com.github.jing332.tts_server_android.help.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.PLAYSTATE_PLAYING
import android.util.Log
import com.github.jing332.tts_server_android.help.audio.AudioDecoder.Companion.readPcmChunk
import java.io.InputStream

class PcmAudioPlayer {
    companion object {
        private const val TAG = "AudioTrackPlayer"
    }

    private var audioTrack: AudioTrack? = null
    private var currentSampleRate = 16000

    @Suppress("DEPRECATION")
    private fun createAudioTrack(sampleRate: Int = 16000): AudioTrack {
        val mSampleRate = if (sampleRate == 0) 16000 else sampleRate
        Log.d(TAG, "createAudioTrack: sampleRate=$mSampleRate")

        val bufferSize = AudioTrack.getMinBufferSize(
            mSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
    }

    suspend fun play(inputStream: InputStream, sampleRate: Int = currentSampleRate) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        inputStream.readPcmChunk(chunkSize = bufferSize) { data ->
            play(data, sampleRate)
        }
    }

    @Synchronized
    fun play(audioData: ByteArray, sampleRate: Int = currentSampleRate) {
        if (currentSampleRate == sampleRate) {
            audioTrack = audioTrack ?: createAudioTrack(sampleRate)
        } else {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = createAudioTrack(sampleRate)
            currentSampleRate = sampleRate
        }

        if (audioTrack!!.playState != PLAYSTATE_PLAYING) audioTrack!!.play()

        audioTrack!!.write(audioData, 0, audioData.size)
        println("play done..")
    }


    fun stop() {
        audioTrack?.stop()
    }

    fun release() {
        audioTrack?.release()
    }
}