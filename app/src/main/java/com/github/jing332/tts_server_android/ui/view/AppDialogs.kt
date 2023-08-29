package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ErrorDialogBinding
import com.github.jing332.tts_server_android.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tts_server_lib.Tts_server_lib

@Suppress("OPT_IN_USAGE")
object AppDialogs {
    fun Context.displayErrorDialog(t: Throwable, title: String? = null) {
        runOnUI {
            val view = FrameLayout(this)
            val binding = ErrorDialogBinding.inflate(layoutInflater, view, true)

            if (!t.localizedMessage.isNullOrBlank())
                binding.tvMsg.text = t.localizedMessage

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

                binding.tvLog.append(span)
                binding.tvLog.append("\n")
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
                    val scope = when (this) {
                        is AppCompatActivity -> this.lifecycleScope
                        else -> GlobalScope
                    }

                    scope.launch(Dispatchers.Main) {
                        kotlin.runCatching {
                            val url = withIO { Tts_server_lib.uploadLog(str) }
                            ClipboardUtils.copyText(url)
                            longToast(R.string.copied)
                        }.onFailure {
                            longToast(getString(R.string.upload_failed, it.message))
                        }
                    }
                }
                .show()
        }
    }
}