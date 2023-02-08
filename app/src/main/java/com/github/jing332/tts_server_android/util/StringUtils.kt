package com.github.jing332.tts_server_android.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object StringUtils {
    private val silentPattern by lazy { Pattern.compile("[\\s\\p{C}\\p{P}\\p{Z}\\p{S}]") }
    private val splitSentencesRegex by lazy { Pattern.compile("[。？?！!;；]") }

    fun formattedDate(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())


    /**
     *  是否为不发音的字符串
     */
    fun isSilent(s: String): Boolean {
        return silentPattern.matcher(s).replaceAll("").isEmpty()
    }

    /**
     * 分割长句并保留分隔符
     */
    fun splitSentences(s: String): List<String> {
        val m = splitSentencesRegex.matcher(s)
        val list = splitSentencesRegex.split(s)
        //保留分隔符
        if (list.isNotEmpty()) {
            var count = 0
            while (count < list.size) {
                if (m.find()) {
                    list[count] += m.group()
                }
                count++
            }
        }

        return list.filter { it.replace("”", "").isNotBlank() }
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

fun String.toJsonListString(): String {
    var s = this.trim().removeSuffix(",")
    if (!startsWith("[")) s = "[$s"
    if (!endsWith("]")) s += "]"
    return s
}

/**
 * 限制字符串长度，过长自动加 "···"
 */
fun String.limitLength(maxLength: Int = 20): String {
    return StringUtils.limitLength(this, maxLength)
}

/**
 * 字符串中汉字数量
 */
fun String.lengthOfChinese(): Int {
    var count = 0
    val c: CharArray = toCharArray()
    for (i in c.indices) {
        val len = Integer.toBinaryString(c[i].code)
        if (len.length > 8) count++
    }
    return count
}

/**
 * 转为html粗体标签
 */
fun String.toHtmlBold(): String {
    return "<b>$this</b>"
}