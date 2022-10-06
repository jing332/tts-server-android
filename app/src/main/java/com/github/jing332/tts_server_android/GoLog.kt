package com.github.jing332.tts_server_android

import android.graphics.Color
import java.io.Serializable

class GoLog(var level: Int, var msg: String) : Serializable {
    fun toText(): String {
        return GoLogLevel.toString(level)
    }

    fun toColor(): Int {
        return when {
            level == GoLogLevel.WarnLevel -> {
                Color.rgb(255, 215, 0) /* 金色 */
            }
            level <= GoLogLevel.ErrorLevel -> {
                Color.RED
            }
            else -> {
                Color.GRAY
            }
        }
    }

}

object GoLogLevel {
    const val PanicLevel = 0
    const val FatalLevel = 1
    const val ErrorLevel = 2
    const val WarnLevel = 3
    const val InfoLevel = 4
    const val DebugLevel = 5
    const val TraceLevel = 6

    fun toString(level: Int): String {
        when (level) {
            PanicLevel -> return "宕机"
            FatalLevel -> return "致命"
            ErrorLevel -> return "错误"
            WarnLevel -> return "警告"
            InfoLevel -> return "信息"
            DebugLevel -> return "调试"
            TraceLevel -> return "详细"
        }
        return level.toString()
    }
}