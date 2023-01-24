package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsLocalEditBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File
import java.util.*

@Parcelize
@Serializable
@SerialName("local")
data class LocalTTS(
    var engine: String? = null,
    var locale: String? = null,
    var voiceName: String? = null,

    var extraParams: MutableList<LocalTtsParameter>? = null,

    var isDirectPlayMode: Boolean = true,

    override var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,
    @Transient
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(isNeedDecode = true)
) : Parcelable, BaseTTS() {
    companion object {
        private const val TAG = "LocalTTS"
        private const val STATUS_INITIALIZING = -2

        private val saveDir by lazy {
            App.context.cacheDir.absolutePath + "/local_tts_audio"
        }

        init {
            kotlin.runCatching {
                File(saveDir).apply { deleteRecursively() }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun getType(): String {
        return App.context.getString(R.string.local)
    }

    override fun getBottomContent(): String {
        return ""
    }

    override fun getDescription(): String {
        val rateStr = if (isRateFollowSystem()) App.context.getString(R.string.follow) else rate
        return "${voiceName ?: App.context.getString(R.string.default_str)} <br>" + App.context.getString(
            R.string.systts_play_params_description,
            "<b>${rateStr}</b>",
            "<b>0</b>",
            "<b>0</b>"
        )
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val binding =
            SysttsLocalEditBottomSheetBinding.inflate(LayoutInflater.from(context), null, false)
                .apply {
                    basicEdit.setData(data)
                    paramsEdit.setData(this@LocalTTS)
                }
        BottomSheetDialog(context).apply {
            setContentView(binding.root)
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                BottomSheetBehavior.from(it).apply {
                    skipCollapsed
                }
            }
            setOnDismissListener { done(data) }
            show()
        }
    }

    @IgnoredOnParcel
    @Transient
    private var mTtsEngine: TextToSpeech? = null

    @IgnoredOnParcel
    @Transient
    private var engineInitStatus: Int = STATUS_INITIALIZING

    @IgnoredOnParcel
    @Transient
    private var waitJob: Job? = null

    @IgnoredOnParcel
    @Transient
    var engineListener: EngineProgressListener? = null

    interface EngineProgressListener {
        fun onStart()
        fun onDone()
    }

    override fun onLoad() {
        Log.i(TAG, "onLoad")

        mTtsEngine?.shutdown()
        engineInitStatus = STATUS_INITIALIZING
        mTtsEngine = TextToSpeech(App.context, {
            engineInitStatus = it
            if (it == TextToSpeech.SUCCESS) {
                mTtsEngine!!.setOnUtteranceProgressListener(object :
                    UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        engineListener?.onStart()
                    }

                    override fun onDone(utteranceId: String?) {
                        engineListener?.onDone()
                        waitJob?.cancel()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                    }

                })
            }
        }, engine)
    }

    override fun onStop() {
        Log.i(TAG, "onStop")
        mTtsEngine?.stop()
        waitJob?.cancel()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        mTtsEngine?.shutdown()
    }

    // return 是否成功
    private suspend fun checkInitAndWait(): Boolean {
        for (i in 1..100) { //5s
            if (mTtsEngine != null && engineInitStatus == TextToSpeech.SUCCESS)
                break
            else if (i == 100)
                return false
            delay(50)
        }
        return true
    }

    private fun setEnginePlayParams(engine: TextToSpeech): Bundle? {
        engine.apply {
            locale?.let { language = Locale.forLanguageTag(it) }
            voiceName?.let { selectedVoice ->
                voices.toList().find { it.name == selectedVoice }?.let {
                    Log.i(TAG, "setVoice: ${it.name}")
                    voice = it
                }
            }

            setSpeechRate((rate - 40) / 10f)
            return Bundle().apply {
                extraParams?.forEach { it.putValueFromBundle(this) }
            }
        }
    }

    override fun directPlay(text: String): Boolean {
        return runBlocking {
            if (!checkInitAndWait()) return@runBlocking false

            waitJob = launch {
                mTtsEngine?.apply {
                    speak(text, TextToSpeech.QUEUE_FLUSH, setEnginePlayParams(this), "")
                }
                awaitCancellation()
            }.job
            waitJob?.start()
            return@runBlocking true
        }
    }

    @IgnoredOnParcel
    private var currentJobId: String? = null

    override fun getAudio(speakText: String): ByteArray? {
        currentJobId = SystemClock.elapsedRealtime().toString()

        File(saveDir).apply { if (!exists()) mkdirs() }
        val file = File("$saveDir/$currentJobId.wav")
        runBlocking {
            checkInitAndWait()
            waitJob = launch {
                mTtsEngine?.apply {
                    synthesizeToFile(speakText, setEnginePlayParams(this), file, currentJobId)
                    // 等待完毕
                    awaitCancellation()
                }
            }.job
            waitJob?.start()
        }

        return if (file.exists()) file.readBytes() else null
    }


    override fun isDirectPlay() = isDirectPlayMode
}