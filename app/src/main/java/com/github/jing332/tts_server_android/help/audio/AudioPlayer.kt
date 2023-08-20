package com.github.jing332.tts_server_android.help.audio

import android.content.Context
import java.io.InputStream

class AudioPlayer(context: Context) {
    private val exoAudioPlayer = ExoAudioPlayer(context)
    private val pcmAudioPlayer = PcmAudioPlayer()

    suspend fun play(inputStream: InputStream, sampleRate: Int) {
        pcmAudioPlayer.play(inputStream, sampleRate)
    }

    fun play(bytes: ByteArray, sampleRate: Int) {
        pcmAudioPlayer.play(bytes, sampleRate)
    }

    suspend fun play(inputStream: InputStream) {
        exoAudioPlayer.play(inputStream)
    }

    suspend fun play(bytes: ByteArray) {
        exoAudioPlayer.play(bytes)
    }

    fun stop() {
        exoAudioPlayer.stop()
        pcmAudioPlayer.stop()
    }

    fun release() {
        exoAudioPlayer.release()
        pcmAudioPlayer.release()
    }
}