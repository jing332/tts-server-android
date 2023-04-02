package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule

class TextReplacer {
    companion object {
        const val TAG = "ReplaceHelper"
    }

    private val rules: MutableList<ReplaceRule> = mutableListOf()

    fun load() {
        rules.clear()
        appDb.replaceRuleDao.allGroupWithReplaceRules().forEach { groupWithRules ->
            rules.addAll(groupWithRules.list.filter { it.isEnabled }.sortedBy { it.order })
        }
    }

    /**
     * 执行替换
     */
    fun replace(text: String): String {
        var s = text
        rules.forEach {
            s = if (it.isRegex)
                s.replace(Regex(it.pattern), it.replacement)
            else
                s.replace(it.pattern, it.replacement)
        }

        return s
    }
}