package com.github.jing332.tts_server_android.ui.systts.edit.microsoft

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isVisible
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsParamsEditViewBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.model.tts.MsTtsAudioFormat
import com.github.jing332.tts_server_android.model.tts.MsTtsFormatManger
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import kotlin.math.max

class MsTtsParamsEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : BaseParamsEditView<SysttsMsParamsEditViewBinding, MsTTS>(context, attrs, defaultStyle),
    Seekbar.OnSeekBarChangeListener {

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
            binding.seekbarStyleDegree.value = tts?.expressAs?.styleDegree ?: 1F
        }

    var callback: Callback? = null

    private val strFollow: String by lazy { context.getString(R.string.follow_system_or_read_aloud_app) }

    var formatValue: String = MsTtsAudioFormat.DEFAULT

    fun setFormatByApi(@MsTtsApiType api: Int, currentFormat: String? = null) {
        mFormatItems = MsTtsFormatManger.getFormatsByApiType(api).map { SpinnerItem(it, it) }
        binding.spinnerFormat.setAdapter(MaterialSpinnerAdapter(context, mFormatItems))

        val format = currentFormat ?: tts?.format
        binding.spinnerFormat.selectedPosition =
            max(mFormatItems.indexOfFirst { it.value == format }, 0)
        tts?.format = mFormatItems[binding.spinnerFormat.selectedPosition].value.toString()
    }

    override var tts: MsTTS? = null

    lateinit var mFormatItems: List<SpinnerItem>

    /**
     * 设置TTS数据 会自动同步值
     */
    override fun setData(tts: MsTTS) {
        super.setData(tts)
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
                tts?.rate = v
            }

            R.id.seekbar_volume -> {
                val v = seekBar.value as Int
                callback?.onVolumeChanged(v)
                tts?.volume = v
            }

            R.id.seekbar_pitch -> {
                val v = seekBar.value as Int
                callback?.onPitchChanged(v)
                tts?.prosody?.pitch = v
            }

            R.id.seekbar_style_Degree -> {
                val v = seekBar.value as Float
                callback?.onStyleDegreeChanged(v)
                tts?.expressAs?.styleDegree = v
            }
        }
    }


    init {
        binding.apply {
            seekbarRate.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarRate.valueFormatter =
                Seekbar.ValueFormatter { value, progress -> if (progress == 0) strFollow else "${value}%" }

            seekbarVolume.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarVolume.valueFormatter = Seekbar.ValueFormatter { value, _ -> "${value}%" }

            seekbarPitch.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarPitch.valueFormatter = seekbarRate.valueFormatter

            seekbarStyleDegree.onSeekBarChangeListener = this@MsTtsParamsEditView
            seekbarStyleDegree.setFloatType(2)
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
                tts?.format = formatValue
                callback?.onFormatChanged(formatValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}