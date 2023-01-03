package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsHttpQuickEditViewBinding
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar

class HttpTtsQuickEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding: SysttsHttpQuickEditViewBinding by lazy {
        SysttsHttpQuickEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        binding.seekBarRate.onSeekBarChangeListener = this
        binding.seekBarVolume.onSeekBarChangeListener = this
    }

    var rate: Int
        get() = binding.seekBarRate.progress
        set(value) {
            mTts.rate = value
            binding.seekBarRate.progress = value
            binding.seekBarRate.textValue = "$value"
        }

    var volume: Int
        get() = binding.seekBarVolume.progress
        set(value) {
            mTts.volume = value
            binding.seekBarVolume.progress = value
            binding.seekBarVolume.textValue =
                if (value == 0) context.getString(R.string.follow_system_or_read_aloud_app) else value.toString()
        }

    private lateinit var mTts: HttpTTS

    fun setData(tts: HttpTTS) {
        mTts = tts
        rate = tts.rate
        volume = tts.volume
    }

    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            binding.seekBarRate -> {
                seekBar.textValue =
                    if (progress == 0) context.getString(R.string.follow_system_or_read_aloud_app) else progress.toString()
            }
            binding.seekBarVolume -> {
                seekBar.textValue = "${binding.seekBarVolume.progress}"
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}

    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
        binding.tvPreviewUrl.text = doAnalyzeUrl()
        mTts.rate = rate
        mTts.volume = volume
    }

    private fun doAnalyzeUrl(): String {
        kotlin.runCatching {
            val a = AnalyzeUrl(
                mUrl = this.mTts.url,
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