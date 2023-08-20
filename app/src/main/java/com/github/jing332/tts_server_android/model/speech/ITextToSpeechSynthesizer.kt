package com.github.jing332.tts_server_android.model.speech

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.InputStream

abstract class ITextToSpeechSynthesizer<T> {
    suspend fun <T> retry(
        times: Int = 3,
        initialDelayMillis: Long = 200,
        factor: Float = 2F,
        maxDelayMillis: Long = 5000,
        onCatch: suspend (times: Int, t: Throwable) -> Boolean,
        block: suspend () -> T?,
    ): T? {
        var currentDelay = initialDelayMillis
        for (i in 1..times) {
            return try {
                block()
            } catch (t: Throwable) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                if (onCatch.invoke(i, t)) continue
                else null
            }
        }
        return null
    }

    open fun load() {}
    open fun stop() {}
    open fun destroy() {}

    open suspend fun handleText(text: String): List<TtsTextSegment> = emptyList()

    open suspend fun getAudio(
        tts: ITextToSpeechEngine,
        text: String,
        sysRate: Int,
        sysPitch: Int
    ): AudioResult? =
        null

    suspend fun synthesizeText(
        tts: ITextToSpeechEngine,
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onAudioAvailable: suspend (AudioData<T>) -> Unit
    ){
        getAudio(tts, text, sysRate, sysPitch)?.let {
            onAudioAvailable.invoke(AudioData(txtTts = TtsTextSegment(tts, text), audio = it, done = {}))
        }
    }

    suspend fun synthesizeText(
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onAudioAvailable: suspend (AudioData<T>) -> Unit
    ) {
        val channel = Channel<AudioData<T>>(10)
        coroutineScope {
            launch(Dispatchers.IO) {
                val textList = handleText(text)
                textList.forEach { subTxtTts ->
                    val audioResult = getAudio(subTxtTts.tts, subTxtTts.text, sysRate, sysPitch)
                    val waitJob = launch { awaitCancellation() }.job
                    channel.send(AudioData(txtTts = subTxtTts, audio = audioResult, done = {
                        waitJob.cancel()
                    }))
                    waitJob.join()
                }
                channel.close()
            }

            for (data in channel) {
                onAudioAvailable.invoke(data)
            }
        }
    }


    data class AudioResult(
        var inputStream: InputStream? = null,
        var bytes: ByteArray? = null,
        var data: Any? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AudioResult

            if (inputStream != other.inputStream) return false
            if (bytes != null) {
                if (other.bytes == null) return false
                if (!bytes.contentEquals(other.bytes)) return false
            } else if (other.bytes != null) return false
            if (data != other.data) return false

            return true
        }

        override fun hashCode(): Int {
            var result = inputStream?.hashCode() ?: 0
            result = 31 * result + (bytes?.contentHashCode() ?: 0)
            result = 31 * result + (data?.hashCode() ?: 0)
            return result
        }
    }

    data class AudioData<T>(
        val txtTts: TtsTextSegment,
        val audio: AudioResult? = null,
        val done: () -> Unit,
    )

}