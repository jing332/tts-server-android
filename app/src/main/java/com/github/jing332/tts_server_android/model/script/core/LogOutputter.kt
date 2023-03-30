package com.github.jing332.tts_server_android.model.script.core

import android.util.Log
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.ui.LogLevel

object LogOutputter {
    var DEBUG = BuildConfig.DEBUG

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
        if (DEBUG) Log.i("Plugin output $level", text.toString())
        targets.forEach { it.appendLog(text, level) }
    }

    fun interface OutputInterface {
        fun appendLog(text: CharSequence, level: Int)
    }
}