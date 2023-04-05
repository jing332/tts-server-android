package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ErrorDialogBinding
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.dp
import com.github.jing332.tts_server_android.util.layoutInflater
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.runOnIO
import com.github.jing332.tts_server_android.util.runOnUI
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tts_server_lib.Tts_server_lib

object AppDialogs {
    fun displayInputDialog(
        context: Context,
        title: String,
        hint: String = "",
        text: String = "",
        onSave: (text: String) -> Unit
    ) {
        val et = MaterialTextInput(context)
        et.hint = hint
        et.editText!!.setText(text)
        et.setPadding(8.dp)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(et)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onSave.invoke(et.editText!!.text.toString())
            }.show()
    }

    fun displayDeleteDialog(context: Context, message: String, onRemove: () -> Unit) {
        MaterialAlertDialogBuilder(context).setTitle(R.string.is_confirm_delete)
            .setMessage(message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                onRemove.invoke()
            }
            .create().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun Context.displayErrorDialog(t: Throwable, title: String? = null) {
        runOnUI {
            val view = FrameLayout(this)
            val binding = ErrorDialogBinding.inflate(layoutInflater, view, true)

            val str = t.stackTraceToString()
            str.lines().forEach {
                val span = if (it.trimStart().startsWith("at")) {
                    SpannableStringBuilder(it).apply {
                        setSpan(
                            StyleSpan(Typeface.ITALIC),
                            0,
                            it.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else {
                    SpannableStringBuilder(it).apply {
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            it.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                binding.tvMessage.append(span)
                binding.tvMessage.append("\n")
            }

            MaterialAlertDialogBuilder(this).apply {
                setTitle(title ?: getString(R.string.error))
            }
                .setIcon(R.drawable.baseline_error_24)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.copy) { _, _ ->
                    ClipboardUtils.copyText(str)
                    toast(R.string.copied)
                }
                .setNegativeButton(R.string.upload_to_url) { _, _ ->
                    GlobalScope.launch(Dispatchers.Main) {
                        kotlin.runCatching {
                            val url = withIO { Tts_server_lib.uploadLog(str) }
                            ClipboardUtils.copyText(url)
                            longToast(R.string.copied)
                        }.onFailure {
                            longToast("上传失败：" + it.message)
                        }
                    }
                }
                .show()
        }
    }

}