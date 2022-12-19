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
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsQuickEditViewBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.model.tts.MsTtsAudioFormat
import com.github.jing332.tts_server_android.model.tts.MsTtsFormatManger
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import kotlin.math.max

class MsTttQuickEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
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
            binding.tvStyleDegree.isVisible = value
            binding.tvValueStyleDegree.isVisible = value
        }

    var callback: Callback? = null

    private val binding: SysttsMsQuickEditViewBinding by lazy {
        SysttsMsQuickEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    var rateValue: Int = 0
    var volumeValue: Int = 0
    var pitchValue: Int = 0
    var styleDegreeValue: Float = 1F
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
        isStyleDegreeVisible = tts.api != MsTtsApiType.EDGE

        setRate(tts.rate)
        setVolume(tts.volume)
        setPitch(tts.pitch)
        tts.expressAs?.let {
            setStyleDegree(it.styleDegree)
        }

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

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.seekbar_rate -> {
                if (progress == 0) {
                    rateValue = MsTTS.RATE_FOLLOW_SYSTEM
                    binding.tvValueRate.setText(R.string.follow_system_or_read_aloud_app)
                } else {
                    rateValue = progress - 100
                    binding.tvValueRate.text = "${rateValue}%"
                }
            }
            R.id.seekbar_volume -> {
                volumeValue = progress - 50
                binding.tvValueVolume.text = "${volumeValue}%"
            }
            R.id.seekbar_pitch -> {
                if (progress == 0) {
                    pitchValue = MsTTS.PITCH_FOLLOW_SYSTEM
                    binding.tvPitchValue.setText(R.string.follow_system_or_read_aloud_app)
                } else {
                    pitchValue = progress - 50
                    binding.tvPitchValue.text = "${pitchValue}%"
                }
            }
            R.id.seekbar_styleDegree -> {
                styleDegreeValue = (progress * 0.01).toFloat()
                binding.tvValueStyleDegree.text = styleDegreeValue.toString()
            }
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