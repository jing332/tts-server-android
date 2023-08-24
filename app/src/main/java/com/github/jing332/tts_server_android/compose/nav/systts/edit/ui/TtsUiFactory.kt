package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

object TtsUiFactory {
    fun from(tts: ITextToSpeechEngine): TtsUI? {
        return when (tts) {
            is PluginTTS -> PluginTtsUI()

            else -> null
        }
    }
}