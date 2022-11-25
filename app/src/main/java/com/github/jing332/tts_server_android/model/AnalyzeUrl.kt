package com.github.jing332.tts_server_android.model

import com.github.jing332.tts_server_android.App
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
        val paramPattern by lazy { Pattern.compile("\\s*,\\s*(?=\\{)") }
        val jsPattern by lazy { Pattern.compile("\\{\\{.*?\\}\\}") }
    }

    fun eval(): UrlOption? {
        // 把http url提取出
        val urlMatcher = paramPattern.matcher(mUrl)
        if (urlMatcher.find()) {
            val start = urlMatcher.start()
            baseUrl = mUrl.substring(0, start)
            val jsonStr = mUrl.substring(start + 1, mUrl.length)

            val urlOption = App.jsonBuilder.decodeFromString<UrlOption>(jsonStr)
            // 提取js替换变量
            val matcher = jsPattern.matcher(urlOption.body.toString())
            val sb = StringBuffer()
            while (matcher.find()) {
                val jsCodeStr =
                    matcher.group().replace("{{", "").replace("}}", "").replace("java.", "")

                val result = evalJs(jsCodeStr)
                matcher.appendReplacement(sb, result.toString())
                matcher.end()
            }
            matcher.appendTail(sb)
            urlOption.body = sb.toString()
            return urlOption
        }
        return null
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