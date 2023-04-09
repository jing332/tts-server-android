package com.github.jing332.tts_server_android.ui.systts.base


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.databinding.SysttsLoggerBottomSheetBinding
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.util.setMarginMatchParent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoggerBottomSheetFragment @JvmOverloads constructor(private val logger: Logger? = null) :
    BottomSheetDialogFragment(), Logger.LogListener {
    companion object {
        const val TAG = "LoggerBottomSheetFragment"
    }

    init {
        logger?.addListener(this)
    }

    private val binding by lazy { SysttsLoggerBottomSheetBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = binding.root

    private val infoColor by lazy {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnBackground,
            typedValue,
            true,
        )
        typedValue.data
    }

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