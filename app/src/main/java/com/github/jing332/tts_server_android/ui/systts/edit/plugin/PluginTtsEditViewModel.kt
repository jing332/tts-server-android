package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.help.plugin.EditUiJsEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.ui.systts.edit.SpinnerData
import java.lang.Integer.max
import java.util.Locale

class PluginTtsEditViewModel : ViewModel() {
    val errMessageLiveData: MutableLiveData<Throwable> by lazy { MutableLiveData() }

    val ui: UiData = UiData()

    private val engine by lazy { EditUiJsEngine(mTts.plugin) }
    private lateinit var mTts: PluginTTS

    fun init(tts: PluginTTS) {
        mTts = tts

        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.locales.selectedItem?.let {
                    mTts.locale = it.value.toString()
                }

                updateVoices()
            }
        }

        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.selectedItem?.let {
                    mTts.voice = it.value.toString()
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
            max(0, ui.locales.items.indexOfFirst { it.value.toString() == mTts.locale })
    }

    private fun updateVoices() {
        val voices = try {
            engine.getVoices(mTts.locale).map { it }
        } catch (e: Exception) {
            errMessageLiveData.postValue(e)
            return
        }

        ui.voices.apply {
            items = voices.map { SpinnerItem(it.value, it.key) }
            position = max(0, voices.indexOfFirst { it.value == mTts.voice })
        }
    }

    data class UiData(
        val locales: SpinnerData = SpinnerData(),
        val voices: SpinnerData = SpinnerData(),
    )
}


