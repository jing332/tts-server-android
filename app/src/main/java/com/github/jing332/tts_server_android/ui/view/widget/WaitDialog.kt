package com.github.jing332.tts_server_android.ui.view.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import com.github.jing332.tts_server_android.databinding.DialogWaitBinding

class WaitDialog(context: Context) : Dialog(context) {
    private val binding = DialogWaitBinding.inflate(layoutInflater)

    init {
        setCanceledOnTouchOutside(false)
        setContentView(binding.root)


        binding.pb.setIndicatorColor(
            *binding.pb.indicatorColor,
            Color.RED,
            Color.YELLOW,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA
        )
    }
}