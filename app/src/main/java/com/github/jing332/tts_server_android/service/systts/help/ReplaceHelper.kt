package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.ReplaceRule

class ReplaceHelper {
    companion object {
        const val TAG = "ReplaceHelper"
    }

    private lateinit var rules: List<ReplaceRule>

    fun load(): String? {
        try {
            rules = appDb.replaceRuleDao.all
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
            if (it.isEnabled) {
                s = if (it.isRegex)
                    s.replace(Regex(it.pattern), it.replacement)
                else
                    s.replace(it.pattern, it.replacement)
            }
        }

        return s
    }
}