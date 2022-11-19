package com.github.jing332.tts_server_android.service.systts.help

import android.util.Log
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import kotlinx.serialization.decodeFromString
import java.io.File

class ReplaceHelper {
    companion object {
        const val TAG = "ReplaceHelper"
    }

    private lateinit var rules: List<ReplaceRuleItemData>

    fun load(): String? {
        try {
            val data = File(AppConst.replaceRulesPath).readText()
            rules = App.jsonBuilder.decodeFromString(data)
            Log.i(TAG, rules.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            rules = arrayListOf()
            return e.message
        }

        return null
    }

    /**
     * 执行替换
     */
    fun doReplace(text: String): String {
        var s = text
        rules.forEach {
            if (it.isEnabled)
                s = s.replace(it.pattern, it.replacement)
        }

        return s
    }
}