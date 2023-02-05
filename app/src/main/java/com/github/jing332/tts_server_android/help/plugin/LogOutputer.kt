package com.github.jing332.tts_server_android.help.plugin

import com.github.jing332.tts_server_android.ui.LogLevel

object LogOutputer {
    private val targets: ArrayList<OutputInterface> = arrayListOf()
    fun addTarget(target: OutputInterface) {
        if (!targets.contains(target)) {
            targets.add(target)
        }
    }

    fun removeTarget(target: OutputInterface) {
        targets.remove(target)
    }

    fun writeLine(text: CharSequence, level: Int = LogLevel.DEBUG) {
        targets.forEach { it.appendLog(text, level) }
    }

    fun interface OutputInterface {
        fun appendLog(text: CharSequence, level: Int)
    }
}