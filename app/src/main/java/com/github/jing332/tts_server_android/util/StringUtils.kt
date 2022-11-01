package com.github.jing332.tts_server_android.util

import java.util.regex.Pattern

object StringUtils {
    private val silentPattern: Pattern = Pattern.compile("[\\s\\p{C}\\p{P}\\p{Z}\\p{S}]")

    /* 是否为不发音的字符串 */
    fun isSilent(s: String): Boolean {
        return silentPattern.matcher(s).replaceAll("").isEmpty()
    }
}