package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import android.content.Context
import android.widget.LinearLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets.BaseViewModel
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

class PluginTtsViewModel : BaseViewModel() {
    lateinit var engine: TtsPluginUiEngine

    fun initEngine(tts: PluginTTS) {
        if (this::engine.isInitialized) return

        engine = TtsPluginUiEngine(tts, app)
    }

    var isLoading by mutableStateOf(true)

    val locales = mutableStateListOf<Pair<String, String>>()
    val voices = mutableStateListOf<Pair<String, String>>()

    suspend fun load(context: Context, tts: PluginTTS, linearLayout: LinearLayout) {
        isLoading = true
        try {
            initEngine(tts)
            withIO { engine.onLoadData() }

            engine.onLoadUI(context, linearLayout)

            updateLocales()
//            updateVoices(tts.locale)
         } catch (t: Throwable) {
            throw t
        } finally {
            isLoading = false
        }
    }

    private fun updateLocales() {
        locales.clear()
        locales.addAll(engine.getLocales().toList())
    }

    fun updateVoices(locale: String) {
        voices.clear()
        voices.addAll(engine.getVoices(locale).toList())
    }

    fun updateCustomUI(locale: String, voice: String) {
        try {
            engine.onVoiceChanged(locale, voice)
        } catch (_: NoSuchMethodException) {
        }
    }
}