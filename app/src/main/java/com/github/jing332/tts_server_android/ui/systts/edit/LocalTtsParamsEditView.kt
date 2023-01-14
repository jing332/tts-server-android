package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsLocalParamsEditViewBinding
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar

class LocalTtsParamsEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0) {
    }

    private val binding: SysttsLocalParamsEditViewBinding by lazy {
        SysttsLocalParamsEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var tts: LocalTTS? = null

    fun setData(tts: LocalTTS) {
        this.tts = tts

        binding.seekbarRate.progress = tts.rate



    }

    init {
        binding.seekbarRate.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: Seekbar) {
                tts?.rate = seekBar.progress
            }
        }
        binding.seekbarRate.valueFormatter = Seekbar.ValueFormatter { value, progress ->
            if (value == BaseTTS.VALUE_FOLLOW_SYSTEM)
                context.getString(R.string.follow_system_or_read_aloud_app)
            else
                value.toString()
        }
    }

}