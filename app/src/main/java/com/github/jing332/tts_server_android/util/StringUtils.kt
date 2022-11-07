package com.github.jing332.tts_server_android.util

import java.util.regex.Pattern

object StringUtils {
    private val silentPattern: Pattern = Pattern.compile("[\\s\\p{C}\\p{P}\\p{Z}\\p{S}]")

    /**
     *  是否为不发音的字符串
     */
    fun isSilent(s: String): Boolean {
        return silentPattern.matcher(s).replaceAll("").isEmpty()
    }

    /**
     * 限制字符串长度，过长自动加 "···"
     */
    fun limitLength(s: String, maxLength: Int): String {
        return if (s.length >= maxLength)
            s.substring(0, maxLength - 1) + "···"
        else s
    }
}

/**
 * 限制字符串长度，过长自动加 "···"
 */
fun String.limitLength(maxLength: Int): String {
    return StringUtils.limitLength(this, maxLength)
}