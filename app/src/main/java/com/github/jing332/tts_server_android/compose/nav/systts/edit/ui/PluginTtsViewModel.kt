package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.base.BaseViewModel
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

class PluginTtsViewModel : BaseViewModel() {
    lateinit var engine: TtsPluginUiEngine

    fun initEngine(tts: PluginTTS) {
        if (this::engine.isInitialized) return

        engine = TtsPluginUiEngine(tts, app)
    }


    val locales = mutableStateListOf<String>()
    val voices = mutableStateListOf<Pair<String, String>>()

    fun initData() {
        locales.clear()
        locales.addAll(engine.getLocales())
    }

    fun onLocaleChanged(locale: String) {
        voices.clear()
        voices.addAll(engine.getVoices(locale).toList())
    }

    fun onVoiceChanged(locale: String, voice: String) {
        engine.onVoiceChanged(locale, voice)
    }
}