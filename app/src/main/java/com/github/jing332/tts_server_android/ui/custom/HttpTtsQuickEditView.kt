package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsHttpQuickEditViewBinding
import com.github.jing332.tts_server_android.model.tts.BaseTTS
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

    var callBack: CallBack? = null

    interface CallBack {
        /*
        * 当值变化时调用
        * @return 解析后的url
        * */
        fun onValueChanged(rate: Int, volume: Int): String
    }

    var rate: Int
        get() {
            return binding.seekBarRate.progress
        }
        set(value) {
            binding.seekBarRate.progress = value
        }

    var volume: Int
        get() {
            return binding.seekBarVolume.progress
        }
        set(value) {
            binding.seekBarVolume.progress = value
        }

    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            binding.seekBarRate -> {
                binding.tvValRate.text =
                    if (progress <= BaseTTS.VALUE_FOLLOW_SYSTEM) context.getString(R.string.follow_system_or_read_aloud_app) else progress.toString()
            }
            binding.seekBarVolume -> {
                binding.tvValVolume.text = "${binding.seekBarVolume.progress}"
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {
    }

    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
        callBack?.onValueChanged(rate, volume)?.let {
            binding.tvPreviewUrl.text = it
        }
    }
}