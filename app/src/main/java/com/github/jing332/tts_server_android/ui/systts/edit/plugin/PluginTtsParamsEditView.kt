package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsPluginParamsEditViewBinding
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

class PluginTtsParamsEditView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defaultStyle: Int = 0) :
    BaseParamsEditView<SysttsPluginParamsEditViewBinding, PluginTTS>(context, attrs, defaultStyle) {

    override var tts: PluginTTS? = null

    override fun setData(tts: PluginTTS) {
        super.setData(tts)

        binding.apply {
            seekRate.valueFormatter = Seekbar.ValueFormatter { _, progress ->
                if (progress == 0) context.getString(R.string.follow_system_or_read_aloud_app)
                else progress.toString()
            }
            seekPitch.valueFormatter = Seekbar.ValueFormatter { _, progress ->
                if (progress == 0) context.getString(R.string.follow_system_or_read_aloud_app)
                else progress.toString()
            }

            seekRate.value = tts.rate
            seekVolume.value = tts.volume
            seekPitch.value = tts.pitch

            seekRate.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
                }

                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    tts.rate = seekBar.value as Int
                }
            }
            seekVolume.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
                }

                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    tts.volume = seekBar.value as Int
                }
            }
            seekPitch.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
                }

                override fun onStopTrackingTouch(seekBar: Seekbar) {
                    tts.pitch = seekBar.value as Int
                }
            }
        }

    }

}