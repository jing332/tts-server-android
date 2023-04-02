package com.github.jing332.tts_server_android.model.speech

abstract class ITextToSpeechSynthesizer<T> {
    lateinit var ttsAdapter: ITextToSpeechAdapter<T>

    open fun load() {
        ttsAdapter.load()
    }

    open fun stop() {
        ttsAdapter.stop()
    }

    open fun destroy() {
        ttsAdapter.destroy()
    }

//    lateinit var audioOutputAdapter: IAudioOutputAdapter

    suspend fun synthesizeText(
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onOutput: suspend (audio: ByteArray?, txtTts: ITextToSpeechAdapter.TextWithTTS<T>) -> Unit,
    ) {
        ttsAdapter.synthesizeTextToAudio(text, sysRate, sysPitch) {
            onOutput.invoke(it.audio, it.txtTts)
        }
    }
}