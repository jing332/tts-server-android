package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ViewHttpTtsQuickEditBinding
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar

class HttpTtsQuickEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val vb by lazy {
        ViewHttpTtsQuickEditBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        vb.seekBarRate.onSeekBarChangeListener = this
        vb.seekBarVolume.onSeekBarChangeListener = this
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
                vb.tvValRate.text =
                    if (progress <= BaseTTS.VALUE_FOLLOW_SYSTEM) context.getString(R.string.follow_system_or_read_aloud_app) else progress.toString()
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