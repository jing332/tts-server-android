package com.github.jing332.tts_server_android.model

import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.AppConst.SCRIPT_ENGINE
import com.script.SimpleBindings
import kotlinx.serialization.decodeFromString
import java.util.regex.Pattern

class AnalyzeUrl(
    val mUrl: String,
    var baseUrl: String = "",
    var speakText: String? = null,
    val speakSpeed: Int? = null,
    val speakVolume: Int? = null,
) {
    companion object {
        val jsPattern: Pattern by lazy { Pattern.compile("\\{\\{.*?\\}\\}") }
    }

    fun eval(): UrlOption? {
        // 提取js替换变量
        val matcher = jsPattern.matcher(mUrl)
        val sb = StringBuffer()
        while (matcher.find()) {
            val jsCodeStr =
                matcher.group().replace("{{", "").replace("}}", "").replace("java.", "")

            kotlin.runCatching {
                val result = evalJs(jsCodeStr)
                matcher.appendReplacement(sb, result.toString())
            }.onFailure {
                throw Exception("执行 $jsCodeStr 时出错", it)
            }

            matcher.end()
        }
        matcher.appendTail(sb)

        val str = sb.toString()
        val splitIndex = str.indexOf(",")
        if (splitIndex < 0) { // GET
            baseUrl = str
            return null
        }

        // POST
        baseUrl = str.substring(0, splitIndex).trim()
        val jsonStr = str.substring(splitIndex + 1)
        return AppConst.jsonBuilder.decodeFromString(jsonStr)
    }


    // 执行js 替换变量
    private fun evalJs(jsStr: String): Any? {
        val bindings = SimpleBindings()
        bindings["speakText"] = speakText
        bindings["speakSpeed"] = speakSpeed
        bindings["speakVolume"] = speakVolume
        return SCRIPT_ENGINE.eval(jsStr, bindings)
    }

    // url中的参数
    @kotlinx.serialization.Serializable
    data class UrlOption(
        var method: String? = null,
        var body: String? = null
    )
}