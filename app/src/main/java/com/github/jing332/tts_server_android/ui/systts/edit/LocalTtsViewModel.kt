package com.github.jing332.tts_server_android.ui.systts.edit

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.runMain
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.max

class LocalTtsViewModel : ViewModel() {
    val ui: UiData = UiData()

    private lateinit var mData: SystemTts
    private lateinit var mTts: LocalTTS

    private lateinit var mAllVoices: List<Voice>


    fun checkAndSetDisplayName(displayName: String) {
        val isAuto = Regex(".*(.*\\..*\\..*)").replace(displayName, "").isBlank()
        mData.displayName = if (isAuto) {
            ui.engines.selectedItem?.displayText
        } else {
            displayName
        }
    }

    fun init(sysTts: SystemTts, onStart: () -> Unit, onDone: () -> Unit) {
        mData = sysTts
        mTts = sysTts.tts as LocalTTS

        ui.engines.addOnPropertyChangedCallback { sender, propertyId ->
            if (propertyId == BR.position) {
                ui.engines.selectedItem?.let { mTts.engine = it.value.toString() }
                viewModelScope.launch {
                    onStart.invoke()


                    val pair = getLanguagesAndVoices(mTts.engine.toString())
                    val locales = pair.first.sortedBy { it.toString() }
                    mAllVoices = pair.second


                    ui.locales.apply {
                        reset()
                        items = locales.map { SpinnerItem(it.displayName, it) }
                        position =
                            max(0, locales.indexOfFirst { it.toLanguageTag() == mTts.locale })
                    }

                    onDone.invoke()
                }
            }
        }

        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.apply {
                    ui.locales.selectedItem?.let {
                        mTts.locale = (it.value as Locale).toLanguageTag()
                    }

                    items = mAllVoices.filter { it.locale.toLanguageTag() == mTts.locale }
                        .sortedBy { it.name }.map {
                            val featureStr =
                                if (it.features.isEmpty()) "" else it.features.toString()
                            SpinnerItem("${it.name} $featureStr", it)
                        }
                    position =
                        max(0, items.indexOfFirst { it.value.toString() == mTts.voiceName })
                }
            }
        }

        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.voices.selectedItem?.let { mTts.voiceName = (it.value as Voice).name }
            }
        }

        ui.engines.items = getEngines().map { SpinnerItem("${it.label} (${it.name})", it.name) }
        val pos = ui.engines.items.indexOfFirst { it.value.toString() == mTts.engine }
        ui.engines.position = max(pos, 0)
    }

    @Suppress("DEPRECATION")
    private fun getEngines(): List<TextToSpeech.EngineInfo> {
        val tts = TextToSpeech(App.context, null)
        val engines = tts.engines
        tts.shutdown()
        return engines
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getLanguagesAndVoices(engine: String): Pair<List<Locale>, List<Voice>> {
        return withIO {
            val latch = CountDownLatch(1)
            var isSuccess = false

            val ttsEngine = TextToSpeech(App.context, {
                isSuccess = it == TextToSpeech.SUCCESS
                latch.countDown()
            }, engine)
            latch.await()

            if (isSuccess) {
                val languages = ttsEngine.availableLanguages.toList()
                val voices = ttsEngine.voices.toList()
                return@withIO Pair(languages, voices)
            }

            ttsEngine.shutdown()
            return@withIO Pair(emptyList(), emptyList())
        }
    }


    var mTestTtsEngine: TextToSpeech? = null

    @Synchronized
    fun doTest(
        text: String,
        onInitFinished: (err: String?) -> Unit,
        onStartPlay: () -> Unit,
        onPlayFinished: () -> Unit,
    ) {
        mTestTtsEngine = TextToSpeech(App.context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                mTestTtsEngine?.setOnUtteranceProgressListener(object :
                    UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        runMain { onStartPlay.invoke() }
                    }

                    override fun onDone(utteranceId: String?) {
                        onPlayFinished.invoke()
                        mTestTtsEngine?.shutdown()
                        mTestTtsEngine = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                    }
                })
                onInitFinished.invoke(null)
                mTestTtsEngine?.apply {
                    setSpeechRate(mTts.rate / 20f)
                    val locale = Locale.forLanguageTag(mTts.locale.toString())
//                    language = locale
                    voices?.toList()?.find { it.locale == locale && it.name == mTts.voiceName }
                        ?.let {
                            setVoice(it)
                        }

                    speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                }
            } else {
                onInitFinished.invoke("TTS Engine initialize failed!")
                mTestTtsEngine = null
            }
        }, mTts.engine)
    }

    fun stopTestPlay() {
        mTestTtsEngine?.stop()
        mTestTtsEngine?.shutdown()
        mTestTtsEngine = null
    }


    data class UiData(
        val engines: SpinnerData = SpinnerData(),
        val locales: SpinnerData = SpinnerData(),
        val voices: SpinnerData = SpinnerData()
    )

}