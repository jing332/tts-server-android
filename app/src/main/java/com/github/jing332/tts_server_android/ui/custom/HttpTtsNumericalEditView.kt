package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.databinding.ViewHttpTtsNumEditBinding
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar

class HttpTtsNumericalEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val vb by lazy {
        ViewHttpTtsNumEditBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        vb.seekBarRate.onSeekBarChangeListener = this
        vb.seekBarVolume.onSeekBarChangeListener = this
    }

    var callBack: CallBack? = null

    interface CallBack {
        fun onValueChanged(rate: Int, volume: Int): String
    }

    var rate: Int
        get() {
            return vb.seekBarRate.progress
        }
        set(value) {
            vb.seekBarRate.progress = value
        }

    var volume: Int
        get() {
            return vb.seekBarVolume.progress
        }
        set(value) {
            vb.seekBarVolume.progress = value
        }

    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            vb.seekBarRate -> {
                vb.tvValRate.text = "${vb.seekBarRate.progress}"
            }
            vb.seekBarVolume -> {
                vb.tvValVolume.text = "${vb.seekBarVolume.progress}"
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {
    }

    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
        callBack?.onValueChanged(rate, volume)?.let {
            vb.tvPreviewUrl.text = it
        }
    }
}