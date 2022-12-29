package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object AppDialogs {
    fun displayRemoveDialog(context: Context, message: String, onRemove: () -> Unit) {
        MaterialAlertDialogBuilder(context).setTitle(R.string.is_confirm_delete)
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ ->
                onRemove.invoke()
            }
            .setFadeAnim().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            }
    }

}