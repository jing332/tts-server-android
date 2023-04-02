package com.github.jing332.tts_server_android.model.speech

import android.util.Log
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine

class TtsSynthesizer : ITextToSpeechSynthesizer<ITextToSpeechEngine>() {
    companion object {
        const val TAG = "MyTtsSynthesizer"
    }

    init {
        ttsAdapter = object : ITextToSpeechAdapter<ITextToSpeechEngine>() {
            init {
                SysTtsLib.setTimeout(5000)
            }

            override suspend fun handleText(text: String): List<TextWithTTS<ITextToSpeechEngine>> {
                Log.i(TAG, "handleText: $text")
                return super.handleText(text)
            }

            override suspend fun getAudio(
                tts: ITextToSpeechEngine,
                text: String,
                sysRate: Int,
                sysPitch: Int
            ): ByteArray? {
                Log.i(TAG, "getAudio $tts, $text")
                return tts.getAudio(text)
            }
        }

    }
}