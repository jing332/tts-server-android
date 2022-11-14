package com.github.jing332.tts_server_android.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ViewSysttsNumericalEditBinding
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar

class SysTtsNumericalEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    interface Callback {
        fun onRateChanged(rate: Int)
        fun onVolumeChanged(volume: Int)
        fun onStyleDegreeChanged(degree: Float)
    }

    var isStyleDegreeVisible: Boolean = true
        set(value) {
            field = value
            binding.seekbarStyleDegree.isVisible = value
            binding.tvStyleDegree.isVisible = value
            binding.tvValueStyleDegree.isVisible = value
        }


    var callback: Callback? = null

    private val binding by lazy {
        ViewSysttsNumericalEditBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    var rateValue: Int = 0
    var volumeValue: Int = 0
    var styleDegreeValue: Float = 1F

    fun setRate(v: Int) {
        binding.seekbarRate.progress = v + 100
    }

    fun setVolume(v: Int) {
        binding.seekbarVolume.progress = v + 50
    }

    fun setStyleDegree(v: Float) {
        binding.seekbarStyleDegree.progress = (v * 100).toInt()
    }

    init {
        binding.seekbarRate.onSeekBarChangeListener = this
        binding.seekbarVolume.onSeekBarChangeListener = this
        binding.seekbarStyleDegree.onSeekBarChangeListener = this
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.seekbar_rate -> {
                rateValue = progress - 100
                if (progress == 0)
                    binding.tvValueRate.setText(R.string.follow_system_or_read_aloud_app)
                else {
                    binding.tvValueRate.text = "${rateValue}%"
                }
            }
            R.id.seekbar_volume -> {
                volumeValue = progress - 50
                binding.tvValueVolume.text = "${volumeValue}%"
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
            }
            R.id.seekbar_volume -> {
                callback?.onVolumeChanged(volumeValue)
            }
            R.id.seekbar_styleDegree -> {
                callback?.onStyleDegreeChanged(styleDegreeValue)
            }
        }
    }
}