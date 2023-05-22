package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.widget.FrameLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsBasicAudioParamsSettingsBinding
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import splitties.systemservices.layoutInflater

class AudioParamsSettingsView(context: Context) : FrameLayout(context) {
    private val binding by lazy {
        SysttsBasicAudioParamsSettingsBinding.inflate(
            layoutInflater, this, true
        )
    }

    private lateinit var mData: AudioParams

    fun setData(audioParams: AudioParams, isGlobal: Boolean = false) {
        mData = audioParams

        binding.apply {
            val formatter = Seekbar.ValueFormatter { value, _ ->
                if (!isGlobal && value == AudioParams.FOLLOW_GLOBAL_VALUE) context.getString(R.string.follow)
                else value.toString()
            }

            seekSpeed.valueFormatter = formatter
            seekVolume.valueFormatter = formatter
            seekPitch.valueFormatter = formatter

            seekSpeed.value = audioParams.speed
            seekVolume.value = audioParams.volume
            seekPitch.value = audioParams.pitch
        }
    }

    init {
        binding.apply {
            seekSpeed.setFloatType(2)
            seekVolume.setFloatType(2)
            seekPitch.setFloatType(2)

            seekSpeed.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    mData.speed = seekBar.value as Float
                }
            }

            seekVolume.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    mData.volume = seekBar.value as Float
                }
            }

            seekPitch.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    mData.pitch = seekBar.value as Float
                }
            }
        }
    }


}