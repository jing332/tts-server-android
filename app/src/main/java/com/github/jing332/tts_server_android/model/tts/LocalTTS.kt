package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
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
import com.github.jing332.tts_server_android.ui.systts.edit.local.LocalTtsEditActivity
import com.github.jing332.tts_server_android.util.toHtmlBold
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
    override var audioPlayer: PlayerParams = PlayerParams(),
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(isNeedDecode = true),
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

    init {
        audioFormat.isNeedDecode = true
    }

    override fun getEditActivity(): Class<out Activity> = LocalTtsEditActivity::class.java

    override fun getType(): String {
        return App.context.getString(R.string.local)
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    override fun getDescription(): String {
        val rateStr = if (isRateFollowSystem()) App.context.getString(R.string.follow) else rate
        return "${voiceName ?: App.context.getString(R.string.default_str)} <br>" + App.context.getString(
            R.string.systts_play_params_description,
            "$rateStr".toHtmlBold(),
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
    private var isInitialized = false

    private fun initEngineIf() {
        if (isInitialized) return
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
                        waitJob?.cancel("onDone")
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                    }

                })
            }
        }, engine)
        isInitialized = true
    }

    @IgnoredOnParcel
    @Transient
    var engineListener: EngineProgressListener? = null

    interface EngineProgressListener {
        fun onStart()
        fun onDone()
    }

    override fun onLoad() {}

    override fun onStop() {
        Log.i(TAG, "onStop")
        mTtsEngine?.stop()
        waitJob?.cancel("onStop")
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
        initEngineIf()
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

    fun getAudioFile(text: String): File {
        initEngineIf()
        currentJobId = SystemClock.elapsedRealtime().toString()

        File(saveDir).apply { if (!exists()) mkdirs() }
        val file = File("$saveDir/$engine.wav")
        runBlocking {
            if (!checkInitAndWait()) {
                throw Exception("Engine initialize failed")
            }

            waitJob = launch {
                mTtsEngine?.apply {
                    synthesizeToFile(text, setEnginePlayParams(this), file, currentJobId)
                    // 等待完毕
                    try {
                        awaitCancellation()
                    } catch (e: CancellationException) {
                        if (e.message == "onStop")
                            kotlin.runCatching { file.delete() }
                    }
                }
            }.job
            waitJob?.start()
        }
        return file
    }

    @IgnoredOnParcel
    private var currentJobId: String? = null

    override fun getAudio(speakText: String): ByteArray? {
        return getAudioFile(speakText).run { if (exists()) readBytes() else null }
    }

    override fun isDirectPlay() = isDirectPlayMode
}