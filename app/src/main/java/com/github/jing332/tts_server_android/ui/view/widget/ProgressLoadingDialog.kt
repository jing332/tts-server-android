package com.github.jing332.tts_server_android.ui.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.databinding.DialogProgressLoadingBinding
import com.github.jing332.tts_server_android.utils.layoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProgressLoadingDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    private val binding = DialogProgressLoadingBinding.inflate(context.layoutInflater)

    override fun create(): AlertDialog {
        setView(binding.root)

        binding.progress.max = 100
        binding.progress.setIndicatorColor(
            *binding.progress.indicatorColor,
            Color.RED,
            Color.YELLOW,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA
        )
        return super.create()
    }

    @SuppressLint("SetTextI18n")
    fun setProgress(progress: Int) {
        binding.tvProgress.text = "${progress}%"
        binding.progress.setProgressCompat(progress, true)
    }

    fun setText(text: String) {
        binding.text.text = text
    }
}