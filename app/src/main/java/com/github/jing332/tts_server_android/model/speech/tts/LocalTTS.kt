package com.github.jing332.tts_server_android.model.speech.tts

import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.utils.toHtmlBold
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.Locale

@Parcelize
@Serializable
@SerialName("local")
data class LocalTTS(
    var engine: String? = null,
    override var locale: String = "",
    var voiceName: String? = null,

    var extraParams: MutableList<LocalTtsParameter>? = null,

    var isDirectPlayMode: Boolean = true,

    override var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,

    override var audioPlayer: PlayerParams = PlayerParams(),
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(isNeedDecode = true),
    override var audioParams: AudioParams = AudioParams(),
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),
) : Parcelable, ITextToSpeechEngine() {
    companion object {
        private const val TAG = "LocalTTS"
        private const val STOP_MESSAGE = "STOP"
        private const val STATUS_INITIALIZING = -2

        private val saveDir by lazy {
            App.context.cacheDir.absolutePath + "/local_tts_audio"
        }

        init {
            kotlin.runCatching {
                File(saveDir).deleteRecursively()
            }
        }
    }

    init {
        audioFormat.isNeedDecode = true
    }

    override fun getType() = App.context.getString(R.string.local)

    override fun getBottomContent() = audioFormat.toString()

    override fun getDescription(): String {
        val rateStr = if (isRateFollowSystem()) App.context.getString(R.string.follow) else rate
        val pitchStr =
            if (isPitchFollowSystem()) App.context.getString(R.string.follow) else pitch / 100f
        return "${voiceName ?: App.context.getString(R.string.default_str)} <br>" + App.context.getString(
            R.string.systts_play_params_description,
            "$rateStr".toHtmlBold(),
            "<b>0</b>",
            "$pitchStr".toHtmlBold()
        )
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
        mTtsEngine = null
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
        waitJob?.cancel(STOP_MESSAGE)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        mTtsEngine?.shutdown()
        mTtsEngine = null
        isInitialized = false
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

    private fun setEnginePlayParams(engine: TextToSpeech, rate: Int, pitch: Int): Bundle {
        engine.apply {
            locale.let { language = Locale.forLanguageTag(it) }
            voiceName?.let { selectedVoice ->
                voices?.toList()?.find { it.name == selectedVoice }?.let {
                    Log.d(TAG, "setVoice: ${it.name}")
                    voice = it
                }
            }

            val r = rate / 10f - 5f // r = -5 ~ +5
            val p = if (pitch <= 0) 1f else pitch / 100f // normal = 1.0
            Log.d(TAG, "setSpeechRate: $r, setPitch: $p")
            setSpeechRate(r)
            setPitch(p)
            return Bundle().apply {
                extraParams?.forEach { it.putValueFromBundle(this) }
            }
        }
    }

    override suspend fun startPlay(text: String, rate: Int, pitch: Int): Boolean = coroutineScope {
        initEngineIf()
        if (!checkInitAndWait()) return@coroutineScope false

        waitJob = launch {
            mTtsEngine?.apply {
                speak(
                    text, TextToSpeech.QUEUE_FLUSH, setEnginePlayParams(this, rate, pitch),
                    ""
                )
            }
            awaitCancellation()
        }.job
        waitJob?.start()
        return@coroutineScope true
    }

    fun getAudioFile(text: String, rate: Int, pitch: Int = 0): File {
        initEngineIf()
        val currentJobId = SystemClock.elapsedRealtime().toString()

        File(saveDir).apply { if (!exists()) mkdirs() }
        val file = File("$saveDir/$engine.wav")
        runBlocking {
            if (!checkInitAndWait()) throw Exception("Engine initialize failed")

            waitJob = launch {
                mTtsEngine?.apply {
                    synthesizeToFile(
                        text,
                        setEnginePlayParams(this, rate, pitch),
                        file,
                        currentJobId
                    )
                    // 等待完毕
                    try {
                        awaitCancellation()
                    } catch (e: CancellationException) {
                        if (e.message == STOP_MESSAGE) // 用户暂停或超时后 删除文件
                            kotlin.runCatching { file.delete() }
                    }
                }
            }.job
            waitJob?.start()
        }
        return file
    }


    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): InputStream {
        return ByteArrayInputStream(
            getAudioFile(speakText, rate, pitch).run { if (exists()) readBytes() else null }
        )
    }

    override fun isDirectPlay() = isDirectPlayMode
}