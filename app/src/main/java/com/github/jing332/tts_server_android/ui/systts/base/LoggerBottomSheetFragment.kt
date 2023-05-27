package com.github.jing332.tts_server_android.ui.systts.base


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.databinding.SysttsLoggerBottomSheetBinding
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.ui.view.Attributes.colorOnBackground
import com.github.jing332.tts_server_android.utils.setMarginMatchParent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoggerBottomSheetFragment @JvmOverloads constructor(private val logger: Logger? = null) :
    BottomSheetDialogFragment(R.layout.systts_logger_bottom_sheet), Logger.LogListener {
    companion object {
        const val TAG = "LoggerBottomSheetFragment"
    }

    init {
        logger?.addListener(this)
    }

    private val binding by viewBinding(SysttsLoggerBottomSheetBinding::bind)

    private val infoColor by lazy { requireContext().colorOnBackground }
    private val channel = Channel<Pair<CharSequence, Int>>(Int.MAX_VALUE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (logger == null) dismiss()
        (view.parent as ViewGroup).setMarginMatchParent()

        lifecycleScope.launch {
            for (data in channel) {
                val span = SpannableString(data.first).apply {
                    val color = if (data.second == LogLevel.INFO)
                        infoColor
                    else
                        LogLevel.toColor(data.second)
                    setSpan(
                        ForegroundColorSpan(color),
                        0, data.first.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                binding.tv.append("\n")
                binding.tv.append(span)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger?.removeListener(this)
        channel.close()
    }

    fun clearLog() {
        if (isAdded) binding.tv.editableText.clear()
    }

    override fun log(text: CharSequence, level: Int) {
        runBlocking { channel.send(text to level) }
    }
}