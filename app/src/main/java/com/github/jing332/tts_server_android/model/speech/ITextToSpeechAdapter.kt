/*
package com.github.jing332.tts_server_android.model.speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

abstract class ITextToSpeechAdapter<T> {
    */
/**
     * @param onCatch return 是否继续重试
     *//*

    suspend fun <T> retry(
        times: Int = 3,
        initialDelayMillis: Long = 500,
        factor: Double = 2.0,
        maxDelayMillis: Long = 3000,
        onCatch: suspend (times: Int, e: Exception) -> Boolean,
        block: suspend () -> T?,
    ): T? {
        var currentDelay = initialDelayMillis
        for (i in 1..times) {
            return try {
                block()
            } catch (e: Exception) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                if (onCatch.invoke(i, e)) continue
                else null
            }
        }
        return null
    }

    open suspend fun handleText(text: String): List<TextWithTTS<T>> {
        return emptyList()
    }

    open suspend fun getAudio(tts: T, text: String, sysRate: Int, sysPitch: Int): ByteArray? = null

    open fun load() {}
    open fun stop() {}
    open fun destroy() {}

    suspend fun synthesizeTextToAudio(
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onAudioAvailable: suspend (AudioData<T>) -> Unit
    ) {
        val channel = Channel<AudioData<T>>(10)

        coroutineScope {
            launch(Dispatchers.IO) {
                val textList = handleText(text)
                textList.forEach { subText ->
                    val audio = getAudio(subText.tts, subText.text, sysRate, sysPitch)
                    channel.send(AudioData(txtTts = subText, audio = audio))
                }
                channel.close()
            }

            for (data in channel) {
                onAudioAvailable.invoke(data)
            }
        }
    }

    data class AudioData<T>(
        val txtTts: TextWithTTS<T>,
        val audio: ByteArray? = null,
        val isCancelled: Boolean = false
    )

    data class TextWithTTS<T>(val tts: T, val text: String)
}*/
