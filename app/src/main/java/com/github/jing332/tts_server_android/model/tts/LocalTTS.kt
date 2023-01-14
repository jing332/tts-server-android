package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
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

@Parcelize
@Serializable
@SerialName("local")
data class LocalTTS(
    var engine: String? = null,

    override var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,
    @Transient
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(isNeedDecode = true)
) : Parcelable, BaseTTS() {
    companion object {
        private const val TAG = "LocalTTS"
        private const val INIT_STATUS_WAIT = -2
    }

    override fun getType(): String {
        return App.context.getString(R.string.local)
    }

    override fun getBottomContent(): String {
        return ""
    }

    override fun getDescription(): String {
        val rateStr = if (isRateFollowSystem()) App.context.getString(R.string.follow) else rate
        return App.context.getString(
            R.string.systts_play_params_description,
            "<b>${rateStr}</b>",
            "<b>暂不支持</b>",
            "<b>暂不支持</b>"
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
    private var engineInitStatus: Int = INIT_STATUS_WAIT

    @IgnoredOnParcel
    @Transient
    private var waitJob: Job? = null

    override fun onLoad() {
        Log.i(TAG, "onLoad")
        mTtsEngine = null
        mTtsEngine = TextToSpeech(App.context, {
            engineInitStatus = it
            if (it == TextToSpeech.SUCCESS) {
                mTtsEngine!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                    }

                    override fun onDone(utteranceId: String?) {
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

    override fun directPlay(text: String) {
        Log.i(TAG, "directPlay: $text")
        runBlocking {
            while (true) {
                delay(100)
                if (mTtsEngine != null && engineInitStatus == TextToSpeech.SUCCESS)
                    break
            }

            waitJob = launch {
                val mRate = rate / 20f
                println(mRate)
                mTtsEngine!!.setSpeechRate(mRate)
                mTtsEngine!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                awaitCancellation()
            }.job
            waitJob?.start()
        }
        Log.i(TAG, "play done")
    }


    override fun isDirectPlay() = true
}