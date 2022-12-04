package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef

@IntDef(ReadAloudTarget.ALL, ReadAloudTarget.ASIDE, ReadAloudTarget.DIALOGUE)
@Retention(AnnotationRetention.SOURCE)
annotation class ReadAloudTarget {
    companion object {
        const val ALL = 0
        const val ASIDE = 1 //旁白
        const val DIALOGUE = 2 //对话

        fun toString(@ReadAloudTarget target: Int): String {
            return when (target) {
                ASIDE -> {
                    "旁白"
                }
                DIALOGUE -> {
                    "对话"
                }
                else -> ""
            }
        }
    }
}