package com.github.jing332.tts_server_android.ui.systts.edit.bgm

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jing332.tts_server_android.databinding.SysttsBgmParamsEditViewBinding
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

class BgmTtsParamsEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), Seekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding by lazy {
        SysttsBgmParamsEditViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun setData(tts: BgmTTS) {
        binding.seekVolume.value = tts.volume
        binding.seekVolume.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: Seekbar) {
                tts.volume = binding.seekVolume.value as Int
            }
        }
    }


}