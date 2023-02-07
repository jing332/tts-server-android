package com.github.jing332.tts_server_android.ui.systts.edit.microsoft

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsParamsEditViewBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.model.tts.MsTtsAudioFormat
import com.github.jing332.tts_server_android.model.tts.MsTtsFormatManger
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import kotlin.math.max

class MsTtsParamsEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), Seekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    interface Callback {
        fun onRateChanged(rate: Int) {}
        fun onVolumeChanged(volume: Int) {}
        fun onPitchChanged(pitch: Int) {}
        fun onStyleDegreeChanged(degree: Float) {}
        fun onFormatChanged(format: String) {}
    }

    var isStyleDegreeVisible: Boolean = true
        set(value) {
            field = value
            binding.seekbarStyleDegree.isVisible = value
            binding.seekbarStyleDegree.value = mTts?.expressAs?.styleDegree ?: 1F
        }

    var callback: Callback? = null

    private val binding: SysttsMsParamsEditViewBinding by lazy {
        SysttsMsParamsEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val strFollow: String by lazy { context.getString(R.string.follow_system_or_read_aloud_app) }

    var formatValue: String = MsTtsAudioFormat.DEFAULT

    fun setFormatByApi(@MsTtsApiType api: Int, currentFormat: String? = null) {
        mFormatItems = MsTtsFormatManger.getFormatsByApiType(api).map { SpinnerItem(it, it) }
        binding.spinnerFormat.setAdapter(MaterialSpinnerAdapter(context, mFormatItems))

        val format = currentFormat ?: mTts?.format
        binding.spinnerFormat.selectedPosition =
            max(mFormatItems.indexOfFirst { it.value == format }, 0)
        mTts?.format = mFormatItems[binding.spinnerFormat.selectedPosition].value.toString()
    }

    private var mTts: MsTTS? = null

    lateinit var mFormatItems: List<SpinnerItem>

    /**
     * 设置TTS数据 会自动同步值
     */
    fun setData(tts: MsTTS) {
        mTts = tts
        setFormatByApi(tts.api)
        binding.apply {
            seekbarRate.value = tts.rate
            seekbarVolume.value = tts.volume
            seekbarPitch.value = tts.pitch

            isStyleDegreeVisible = tts.expressAs?.style?.isNotEmpty() == true
            if (isStyleDegreeVisible) seekbarStyleDegree.value = tts.expressAs?.styleDegree ?: 1F
        }

    }

    override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {

    }

    override fun onStopTrackingTouch(seekBar: Seekbar) {
        when (seekBar.id) {
            R.id.seekbar_rate -> {
                val v = seekBar.value as Int
                callback?.onRateChanged(v)
                mTts?.rate = v
            }
            R.id.seekbar_volume -> {
                val v = seekBar.value as Int
                callback?.onVolumeChanged(v)
                mTts?.volume = v
            }
            R.id.seekbar_pitch -> {
                val v = seekBar.value as Int
                callback?.onPitchChanged(v)
                mTts?.prosody?.pitch = v
            }
            R.id.seekbar_style_Degree -> {
                val v = seekBar.value as Float
                callback?.onStyleDegreeChanged(v)
                mTts?.expressAs?.styleDegree = v
            }
        }
    }


    init {
        binding.apply {
            seekbarRate.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarRate.valueFormatter =
                Seekbar.ValueFormatter { value, progress -> if (progress == 0) strFollow else "${value}%" }
            seekbarRate.progressConverter = object : Seekbar.ProgressConverter {
                override fun valueToProgress(value: Any) = (value as Int) + 100
                override fun progressToValue(progress: Int) = progress - 100
            }

            seekbarVolume.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarVolume.valueFormatter = Seekbar.ValueFormatter { value, _ -> "${value}%" }
            seekbarVolume.progressConverter = object : Seekbar.ProgressConverter {
                override fun valueToProgress(value: Any) = (value as Int) + 50
                override fun progressToValue(progress: Int) = progress - 50
            }

            seekbarPitch.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarPitch.valueFormatter =
                Seekbar.ValueFormatter { value, progress -> if (progress == 0) strFollow else "${value}%" }
            seekbarPitch.progressConverter = object : Seekbar.ProgressConverter {
                override fun valueToProgress(value: Any) = (value as Int) + 50
                override fun progressToValue(progress: Int) = progress - 50
            }

            seekbarStyleDegree.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarStyleDegree.progressConverter = object : Seekbar.ProgressConverter {
                override fun valueToProgress(value: Any) = ((value as Float) * 100).toInt()
                override fun progressToValue(progress: Int) = (progress * 0.01).toFloat()
            }
        }



        binding.seekbarPitch.onSeekBarChangeListener = this

        binding.spinnerFormat.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                formatValue = mFormatItems[position].value.toString()
                mTts?.format = formatValue
                callback?.onFormatChanged(formatValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}