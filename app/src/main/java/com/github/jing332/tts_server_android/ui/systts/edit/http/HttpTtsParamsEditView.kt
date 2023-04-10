package com.github.jing332.tts_server_android.ui.systts.edit.http

import android.content.Context
import android.util.AttributeSet
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsHttpParamsEditViewBinding
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.model.speech.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

class HttpTtsParamsEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defaultStyle: Int = 0
) :
    BaseParamsEditView<SysttsHttpParamsEditViewBinding, HttpTTS>(context, attrs, defaultStyle),
    Seekbar.OnSeekBarChangeListener {
    init {
        binding.seekBarRate.onSeekBarChangeListener = this
        binding.seekBarVolume.onSeekBarChangeListener = this

        binding.seekBarRate.valueFormatter =
            Seekbar.ValueFormatter { _, progress ->
                if (progress == 0) context.getString(R.string.follow_system_or_read_aloud_app)
                else progress.toString()
            }
    }

    var rate: Int
        get() = binding.seekBarRate.value as Int
        set(value) {
            binding.seekBarRate.value = value
        }

    private var volume: Int
        get() = binding.seekBarVolume.value as Int
        set(value) {
            binding.seekBarVolume.value = value
        }


    override var tts: HttpTTS? = null

    override fun setData(tts: HttpTTS) {
        super.setData(tts)
        this.tts = tts
        rate = tts.rate
        volume = tts.volume
    }

    override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: Seekbar) {}

    override fun onStopTrackingTouch(seekBar: Seekbar) {
        binding.tvPreviewUrl.text = doAnalyzeUrl()
        tts?.let {
            it.rate = rate
            it.volume = volume
        }
    }

    private fun doAnalyzeUrl(): String {
        kotlin.runCatching {
            val a = AnalyzeUrl(
                mUrl = this.tts!!.url,
                speakSpeed = rate,
                speakVolume = volume
            )
            val result = a.eval()
            return result?.body ?: a.baseUrl
        }.onFailure {
            return "${it.message}"
        }
        return ""
    }

}