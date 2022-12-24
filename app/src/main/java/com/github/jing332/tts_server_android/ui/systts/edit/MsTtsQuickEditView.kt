package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsQuickEditViewBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.model.tts.MsTtsAudioFormat
import com.github.jing332.tts_server_android.model.tts.MsTtsFormatManger
import com.github.jing332.tts_server_android.ui.custom.adapter.initAccessibilityDelegate
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import kotlin.math.max

object MsTtsQuickEditViewAdapter {
    @JvmStatic
    @BindingAdapter("styleDegreeVisible")
    fun setStyleDegreeVisible(view: View, visible: Boolean) {
        if (view is MsTtsQuickEditView)
            view.isStyleDegreeVisible = visible
    }
}

class MsTtsQuickEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
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
        }

    var callback: Callback? = null

    private val binding: SysttsMsQuickEditViewBinding by lazy {
        SysttsMsQuickEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val strFollow: String by lazy { context.getString(R.string.follow_system_or_read_aloud_app) }

    private var rateValue: Int = 0
        set(value) {
            field = value
            binding.seekbarRate.textValue =
                if (value == MsTTS.RATE_FOLLOW_SYSTEM) strFollow else "${value}%"
        }

    private var volumeValue: Int = 0
        set(value) {
            field = value
            binding.seekbarVolume.textValue = "${value}%"
        }

    private var pitchValue: Int = 0
        set(value) {
            field = value
            binding.seekbarPitch.textValue =
                if (value == MsTTS.PITCH_FOLLOW_SYSTEM) strFollow else "${value}%"
        }

    private var styleDegreeValue: Float = 1F
        set(value) {
            field = value
            binding.seekbarStyleDegree.textValue = "$value"
        }

    var formatValue: String = MsTtsAudioFormat.DEFAULT

    private fun setRate(v: Int) {
        binding.seekbarRate.progress = v + 100
    }

    private fun setVolume(v: Int) {
        binding.seekbarVolume.progress = v + 50
    }

    private fun setPitch(v: Int) {
        binding.seekbarPitch.progress = v + 50
    }

    private fun setStyleDegree(v: Float) {
        binding.seekbarStyleDegree.progress = (v * 100).toInt()
    }

//    // 解决 progress 相同时不回调 onProgressChanged()
//    private fun setProgress(seekBar: ConvenientSeekbar, progress: Int) {
//        if (seekBar.progress == progress) {
//            onProgressChanged(seekBar, progress, false)
//        } else
//            seekBar.progress = progress
//    }

    fun setFormatByApi(@MsTtsApiType api: Int, currentFormat: String? = null) {
        mFormatItems = MsTtsFormatManger.getFormatsByApiType(api).map { SpinnerItem(it, it) }
        binding.spinnerFormat.setAdapter(MaterialSpinnerAdapter(context, mFormatItems))

        val format = currentFormat ?: mTts?.format
        binding.spinnerFormat.selectedPosition =
            max(mFormatItems.indexOfFirst { it.value == format }, 0)
    }

    private var mTts: MsTTS? = null

    lateinit var mFormatItems: List<SpinnerItem>

    /**
     * 设置TTS数据 会自动同步值
     */
    fun setData(tts: MsTTS) {
        mTts = tts
        setRate(tts.rate)
        setVolume(tts.volume)
        setPitch(tts.pitch)

        isStyleDegreeVisible = tts.expressAs?.style?.isNotEmpty() == true
        if (isStyleDegreeVisible) setStyleDegree(tts.expressAs?.styleDegree ?: 1F)

        mFormatItems = MsTtsFormatManger.getFormatsByApiType(tts.api).map { SpinnerItem(it, it) }
        binding.spinnerFormat.setAdapter(MaterialSpinnerAdapter(context, mFormatItems))
        binding.spinnerFormat.selectedPosition =
            max(mFormatItems.indexOfFirst { it.value == tts.format }, 0)
    }

    init {
        binding.seekbarRate.onSeekBarChangeListener = this
        binding.seekbarVolume.onSeekBarChangeListener = this
        binding.seekbarPitch.onSeekBarChangeListener = this
        binding.seekbarStyleDegree.onSeekBarChangeListener = this

        binding.tilFormat.initAccessibilityDelegate()
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

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.seekbar_rate -> rateValue =
                if (progress == 0) MsTTS.RATE_FOLLOW_SYSTEM else progress - 100

            R.id.seekbar_volume -> volumeValue = progress - 50
            R.id.seekbar_pitch -> pitchValue =
                if (progress == 0) MsTTS.PITCH_FOLLOW_SYSTEM else progress - 50

            R.id.seekbar_styleDegree -> styleDegreeValue = (progress * 0.01).toFloat()
        }
    }

    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}

    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
        when (seekBar.id) {
            R.id.seekbar_rate -> {
                callback?.onRateChanged(rateValue)
                mTts?.let { it.rate = rateValue }
            }
            R.id.seekbar_volume -> {
                callback?.onVolumeChanged(volumeValue)
                mTts?.let { it.volume = volumeValue }
            }
            R.id.seekbar_pitch -> {
                callback?.onPitchChanged(pitchValue)
                mTts?.prosody?.let { it.pitch = pitchValue }
            }
            R.id.seekbar_styleDegree -> {
                callback?.onStyleDegreeChanged(styleDegreeValue)
                mTts?.expressAs?.let { it.styleDegree = styleDegreeValue }
            }
        }
    }
}