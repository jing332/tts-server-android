package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.SpinnerData
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.runOnIO
import java.lang.Integer.max
import java.util.Locale

class PluginTtsEditViewModel(application: Application) : AndroidViewModel(application) {
    val errMessageLiveData: MutableLiveData<Throwable> by lazy { MutableLiveData() }

    val ui: UiData = UiData()

    lateinit var tts: PluginTTS
    val engine by lazy { TtsPluginUiEngine(tts, getApplication()) }

    fun checkDisplayName(name: String): String {
        return name.ifBlank { ui.voices.selectedItem?.displayText ?: name }
    }

    fun init() {
        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.locales.selectedItem?.let {
                    tts.locale = it.value.toString()
                }

                updateVoices()
            }
        }

        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.selectedItem?.let {
                    tts.voice = it.value.toString()
                    try {
                        engine.onVoiceChanged(tts.locale, tts.voice)
                    } catch (_: NoSuchMethodException) {
                    } catch (t: Throwable) {
                        errMessageLiveData.postValue(t)
                    }

                }
            }
        }

        kotlin.runCatching {
            ui.locales.items = engine.getLocales().map {
                val loc = Locale.forLanguageTag(it)
                SpinnerItem(loc.getDisplayName(loc), it)
            }
        }.onFailure {
            errMessageLiveData.postValue(it)
            return
        }

        ui.locales.position =
            max(0, ui.locales.items.indexOfFirst { it.value.toString() == tts.locale })
    }

    @Suppress("RemoveSingleExpressionStringTemplate")
    private fun updateVoices() {
        val voices = try {
            engine.getVoices(tts.locale).map { it }
        } catch (e: Exception) {
            errMessageLiveData.postValue(e)
            return
        }

        ui.voices.apply {
            items = voices.map { SpinnerItem(it.value, it.key) }
            position = //it.key.toString() 不能从Integer转换??
                max(0, voices.indexOfFirst { "${it.key}" == tts.voice })
        }
    }

    fun doTest(
        text: String,
        onSuccess: suspend (audio: ByteArray, sampleRate: Int, mime: String) -> Unit,
        onFailure: suspend (Throwable) -> Unit
    ) {
        viewModelScope.runOnIO {
            val audio = try {
                tts.onLoad()
                tts.getAudioWithSystemParams(text)
            } catch (e: Exception) {
                withMain { onFailure.invoke(e) }
                return@runOnIO
            }

            if (audio == null) {
                withMain { onFailure.invoke(Exception("null")) }
                return@runOnIO
            }
            val ret = AudioDecoder.getSampleRateAndMime(audio)

            withMain { onSuccess(audio, ret.first, ret.second) }
        }
    }

    data class UiData(
        val locales: SpinnerData = SpinnerData(),
        val voices: SpinnerData = SpinnerData(),
    )
}


