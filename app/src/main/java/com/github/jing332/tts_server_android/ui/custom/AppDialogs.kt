package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.runOnIO
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import tts_server_lib.Tts_server_lib

object AppDialogs {
    fun displayDeleteDialog(context: Context, message: String, onRemove: () -> Unit) {
        MaterialAlertDialogBuilder(context).setTitle(R.string.is_confirm_delete)
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ ->
                onRemove.invoke()
            }
            .create().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            }
    }

    fun displayExportDialog(context: Context, scope: CoroutineScope, json: String) {
        context.apply {
            val tv = TextView(this).apply {
                text = json
                setTextIsSelectable(true)
                setPadding(50, 50, 50, 0)
            }
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.export_config)
                .setView(tv)
                .setPositiveButton(R.string.copy) { _, _ ->
                    ClipboardUtils.copyText(json)
                    toast(R.string.copied)
                }.setNegativeButton(getString(R.string.upload_to_url)) { _, _ ->
                    scope.runOnIO {
                        kotlin.runCatching {
                            val url = updateToUrl(json)
                            ClipboardUtils.copyText(url)
                            longToast("${getString(R.string.copied)}: \n${url}")
                        }.onFailure {
                            longToast(getString(R.string.upload_failed, it.message))
                        }
                    }
                }

                .show()
        }
    }

    private fun updateToUrl(json: String): String {
        return Tts_server_lib.uploadConfig(json)
    }

}