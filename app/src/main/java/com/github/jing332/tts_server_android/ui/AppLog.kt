package com.github.jing332.tts_server_android.ui

import android.os.Parcelable
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.constant.LogLevel.Companion.toColor
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppLog(@LogLevel val level: Int, val msg: String) : Parcelable {
    fun toColor(): Int {
        return toColor(level)
    }
}