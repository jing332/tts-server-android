package com.github.jing332.tts_server_android.constant

import android.graphics.Color
import androidx.annotation.IntDef

@IntDef(
    LogLevel.PANIC,
    LogLevel.FATIL,
    LogLevel.ERROR,
    LogLevel.WARN,
    LogLevel.INFO,
    LogLevel.DEBUG,
    LogLevel.TRACE
)
annotation class LogLevel {
    companion object {
        const val PANIC = 0
        const val FATIL = 1
        const val ERROR = 2
        const val WARN = 3
        const val INFO = 4
        const val DEBUG = 5
        const val TRACE = 6

        fun toColor(level: Int): Int {
            return when {
                level == WARN -> {
                    Color.rgb(255, 215, 0) /* 金色 */
                }

                level <= ERROR -> {
                    Color.RED
                }

                else -> {
                    Color.GRAY
                }
            }
        }

        fun toString(level: Int): String {
            when (level) {
                PANIC -> return "宕机"
                FATIL -> return "致命"
                ERROR -> return "错误"
                WARN -> return "警告"
                INFO -> return "信息"
                DEBUG -> return "调试"
                TRACE -> return "详细"
            }
            return level.toString()
        }
    }
}