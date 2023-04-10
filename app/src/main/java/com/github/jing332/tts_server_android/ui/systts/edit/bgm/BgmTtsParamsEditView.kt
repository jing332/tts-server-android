package com.github.jing332.tts_server_android.ui.systts.edit.bgm

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsBgmParamsEditViewBinding
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

class BgmTtsParamsEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defaultStyle: Int = 0
) :
    BaseParamsEditView<SysttsBgmParamsEditViewBinding, BgmTTS>(context, attrs, defaultStyle),
    Seekbar.OnSeekBarChangeListener {

    override fun setData(tts: BgmTTS) {
        super.setData(tts)

        binding.seekVolume.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: Seekbar) {
                tts.volume = binding.seekVolume.value as Int
            }
        }
        binding.seekVolume.valueFormatter = Seekbar.ValueFormatter { value, _ ->
            if (value == 0) context.getString(R.string.follow) else value.toString()
        }
        binding.seekVolume.value = tts.volume
    }
}