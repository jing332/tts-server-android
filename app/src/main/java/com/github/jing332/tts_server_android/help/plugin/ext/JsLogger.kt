@file:Suppress("unused")

package com.github.jing332.tts_server_android.help.plugin.ext

import androidx.annotation.Keep
import com.github.jing332.tts_server_android.help.plugin.LogOutputter
import com.github.jing332.tts_server_android.ui.LogLevel
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject

@Keep
interface JsLogger {
    fun d(obj: Any) {
        LogOutputter.writeLine(jsObj2String(obj), LogLevel.DEBUG)
    }

    fun i(obj: Any) {
        LogOutputter.writeLine(jsObj2String(obj), LogLevel.INFO)
    }

    fun w(obj: Any) {
        LogOutputter.writeLine(jsObj2String(obj), LogLevel.WARN)
    }

    fun e(obj: Any) {
        LogOutputter.writeLine(jsObj2String(obj), LogLevel.ERROR)
    }

    fun jsObj2String(obj: Any): String {
        return when (obj) {
            is NativeArray -> obj.show
            is NativeObject -> obj.show()
            is ByteArray -> obj.contentToString()
            else -> obj.toString()
        }
    }

    val NativeArray.show
        get() = this.toArray().joinToString(prefix = "[", postfix = "]")

    fun NativeObject.show(
        deep: Int = 1
    ): String {
        val stringBuilder = StringBuilder("{\n")
        this.ids.forEach { id ->
            if (id is String) {
                val v = this[id]
                val value = when (v) {
                    is NativeObject -> v.show(deep + 1)
                    is NativeArray -> v.show
                    else -> v?.javaClass?.name
                }
                //println("$id->${this[id]?.javaClass?.name}")
                for (i in 0 until deep) stringBuilder.append("\t")
                stringBuilder.append("${id}:${value},\n") //删去多余的","
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length - 2)
        for (i in 0 until deep - 1) stringBuilder.append("\t")
        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}