package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.ui.systts.edit.local.TtsEngineHelper
import java.util.Locale

class LocalTtsViewModel : ViewModel() {
    private val engineHelper by lazy { TtsEngineHelper(App.context, viewModelScope) }

    val engines = mutableStateListOf<TextToSpeech.EngineInfo>()
    val locales = mutableStateListOf<Locale>()
    val voices = mutableStateListOf<Voice>()

    fun init() {
        engines.clear()
        engines.addAll(TtsEngineHelper.getEngines())
    }

    suspend fun setEngine(engine: String) {
        engineHelper.setEngine(engine)

        engines.clear()
        engines.addAll(TtsEngineHelper.getEngines())
    }

    fun updateLocales() {
        locales.clear()
        locales.addAll(engineHelper.locales)
    }

    fun updateVoices(locale: String) {
        voices.clear()
        voices.addAll(engineHelper.voices.filter { it.locale.toLanguageTag() == locale }
            .sortedBy { it.name })
    }


    override fun onCleared() {
        super.onCleared()

        engineHelper.shutdown()
    }
}