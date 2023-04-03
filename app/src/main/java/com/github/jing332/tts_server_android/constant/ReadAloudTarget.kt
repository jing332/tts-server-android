package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app

@IntDef(
    ReadAloudTarget.ALL,
    ReadAloudTarget.ASIDE,
    ReadAloudTarget.DIALOGUE,
    ReadAloudTarget.BGM,
    ReadAloudTarget.CUSTOM_TAG
)
@Retention(AnnotationRetention.SOURCE)
annotation class ReadAloudTarget {
    companion object {
        const val ALL = 0
        const val ASIDE = 1 //旁白
        const val DIALOGUE = 2 //对话
        const val BGM = 3 //背景音乐
        const val CUSTOM_TAG = 4 // 自定义Tag

        fun toText(@ReadAloudTarget target: Int): String {
            return when (target) {
                ASIDE -> {
                    App.context.getString(R.string.aside)
                }

                DIALOGUE -> {
                    App.context.getString(R.string.dialogue)
                }

                BGM -> {
                    app.getString(R.string.bgm)
                }

                else -> ""
            }
        }
    }
}