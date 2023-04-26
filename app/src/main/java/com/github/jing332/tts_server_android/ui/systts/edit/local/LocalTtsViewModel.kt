package com.github.jing332.tts_server_android.ui.systts.edit.local

import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.model.speech.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.systts.edit.SpinnerData
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.utils.runOnIO
import com.github.jing332.tts_server_android.utils.runOnUI
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.math.max

class LocalTtsViewModel : ViewModel() {
    companion object {
        // 默认的SpinnerItem 用于风格和角色
        private val DEFAULT_SPINNER_ITEM: SpinnerItem by lazy {
            SpinnerItem(App.context.getString(R.string.default_str), null)
        }

        private val STR_DEFAULT by lazy { App.context.getString(R.string.default_str) }
    }

    val voiceEnabledLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val ui: UiData = UiData()

    private val engineHelper by lazy { TtsEngineHelper(App.context, viewModelScope) }

    private lateinit var mData: SystemTts
    private lateinit var mTts: LocalTTS

    private lateinit var mAllVoices: List<Voice>


    fun checkAndSetDisplayName(displayName: String) {
        val isAuto = Regex(".*(.*\\..*\\..*)").replace(displayName, "").isBlank()
        mData.displayName = if (isAuto) {
            ui.engines.selectedItem?.displayText ?: ""
        } else {
            displayName
        }
    }

    fun init(sysTts: SystemTts, onStart: () -> Unit, onDone: (ok: Boolean) -> Unit) {
        mData = sysTts
        mTts = sysTts.tts as LocalTTS

        ui.engines.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.engines.selectedItem?.let {
                    mTts.engine = it.value.toString()
                    mTts.onLoad()
                }

                viewModelScope.runOnIO {
                    withMain { onStart.invoke() }

                    val ok = withTimeoutOrNull(8000) {
                        engineHelper.setEngine(mTts.engine!!)
                    } ?: false

                    if (!ok) { // 超时
                        withMain { onDone(false) }
                        return@runOnIO
                    }

                    mAllVoices = engineHelper.voices
                    updateLocale(engineHelper.locales)

                    withMain { onDone.invoke(true) }
                }
            }
        }

        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.apply {
                    ui.locales.selectedItem?.let {
                        mTts.locale = if (it.value == null) {
                            mTts.voiceName = null
                            null
                        } else {
                            (it.value as Locale).toLanguageTag()
                        }
                    }
                    updateVoice(mAllVoices)

                    voiceEnabledLiveData.postValue(mTts.locale != null)
                }
            }
        }

        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.selectedItem?.let {
                    mTts.voiceName = if (it.value == null)
                        null
                    else (it.value as Voice).name
                }
            }
        }

        ui.engines.items = getEngines().map { SpinnerItem("${it.label} (${it.name})", it.name) }
        val pos = ui.engines.items.indexOfFirst { it.value.toString() == mTts.engine }
        ui.engines.position = max(pos, 0)
    }

    // 更新地区
    private fun updateLocale(locales: List<Locale>) {
        ui.locales.apply {
            reset()
            items = locales.map { SpinnerItem(it.displayName, it) }
                .toMutableList()
                .apply { add(0, DEFAULT_SPINNER_ITEM) }

            val userPos = max(0, items.indexOfFirst {
                it.value != null && (it.value as Locale).toLanguageTag() == mTts.locale
            })
            position = max(0, if (userPos == 0) {
                val default = Locale.getDefault()
                items.indexOfFirst {
                    if (it.value == null) false else {
                        val locale = it.value as Locale
                        locale.language == default.language && locale.country == default.country
                    }
                }
            } else userPos)
        }
    }

    // 更新语音
    private fun updateVoice(voices: List<Voice>) {
        ui.voices.apply {
            reset()
            items = voices.filter { it.locale.toLanguageTag() == mTts.locale }
                .sortedBy { it.name }.map {
                    val featureStr =
                        if (it.features == null || it.features.isEmpty() == true) "" else it.features.toString()
                    SpinnerItem("${it.name} $featureStr", it)
                }

            position =
                max(0, items.indexOfFirst {
                    it.value != null && (it.value as Voice).name == mTts.voiceName
                })
        }
    }

    @Suppress("DEPRECATION")
    private fun getEngines(): List<TextToSpeech.EngineInfo> {
        val tts = TextToSpeech(App.context, null)
        val engines = tts.engines
        tts.shutdown()
        return engines
    }

    @Synchronized
    fun doTest(
        text: String,
        onEngineInitSuccess: () -> Unit,

        onGetAudioSuccess: suspend (audio: ByteArray, sampleRate: Int) -> Unit
    ) {
        viewModelScope.launch {
            mTts.onLoad()

            mTts.engineListener = null
            if (mTts.isDirectPlayMode) {
                mTts.engineListener = object : LocalTTS.EngineProgressListener {
                    override fun onStart() {
                        runOnUI { onEngineInitSuccess.invoke() }
                    }

                    override fun onDone() {
                    }
                }
                withIO { mTts.startPlayWithSystemParams(text) }
            } else {
                withIO {
                    mTts.getAudioWithSystemParams(text)?.let {
                        val sampleRate = AudioDecoder.getSampleRateAndMime(it.readBytes()).first

                        withMain { onGetAudioSuccess(it.readBytes(), sampleRate) }
                        return@withIO
                    }
                }
            }
        }
    }

    fun stopTestPlay() {
        mTts.onStop()
    }

    override fun onCleared() {
        super.onCleared()
        mTts.onDestroy()
        engineHelper.shutdown()
    }


    data class UiData(
        val engines: SpinnerData = SpinnerData(),
        val locales: SpinnerData = SpinnerData(),
        val voices: SpinnerData = SpinnerData()
    )

}
