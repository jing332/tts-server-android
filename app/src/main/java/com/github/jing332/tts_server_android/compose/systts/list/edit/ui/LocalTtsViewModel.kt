package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.model.LocalTtsEngine
import java.util.Locale

class LocalTtsViewModel : ViewModel() {
    private val engine by lazy { LocalTtsEngine(App.context) }

    val engines = mutableStateListOf<TextToSpeech.EngineInfo>()
    val locales = mutableStateListOf<Locale>()
    val voices = mutableStateListOf<Voice>()

    fun init() {
        engines.clear()
        engines.addAll(LocalTtsEngine.getEngines())
    }

    suspend fun setEngine(engine: String) {
        val ok = this.engine.setEngine(engine)
        if (!ok) return

        engines.clear()
        engines.addAll(LocalTtsEngine.getEngines())
    }

    fun updateLocales() {
        locales.clear()
        locales.addAll(engine.locales)
    }

    fun updateVoices(locale: String) {
        voices.clear()
        voices.addAll(engine.voices
            .filter { it.locale.toLanguageTag() == locale }
            .sortedBy { it.name }
        )
    }

    override fun onCleared() {
        super.onCleared()

        engine.shutdown()
    }
}