package com.github.jing332.tts_server_android.ui.systts.plugin.debug

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.databinding.SysttsPluginLoggerBottomSheetBinding
import com.github.jing332.tts_server_android.help.plugin.LogOutputter
import com.github.jing332.tts_server_android.ui.LogLevel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PluginLoggerBottomSheetFragment : BottomSheetDialogFragment(), LogOutputter.OutputInterface {
    init {
        LogOutputter.addTarget(this)
    }

    private val binding by lazy { SysttsPluginLoggerBottomSheetBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    private val channel = Channel<SpannableString>(Int.MAX_VALUE)

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogOutputter.addTarget(this)
        binding.root.minimumHeight = requireActivity().windowManager.defaultDisplay.height

        lifecycleScope.launch {
            for (span in channel) {
                withMain {
                    binding.tv.append("\n")
                    binding.tv.append(span)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogOutputter.removeTarget(this)
        channel.close()
    }

    override fun appendLog(text: CharSequence, level: Int) {
        synchronized(this) {
            val span = SpannableString(text).apply {
                setSpan(
                    ForegroundColorSpan(LogLevel.toColor(level)),
                    0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            runBlocking { channel.send(span) }
        }
    }

    fun clearLog() {
        if (isAdded)
            binding.tv.editableText.clear()
    }
}