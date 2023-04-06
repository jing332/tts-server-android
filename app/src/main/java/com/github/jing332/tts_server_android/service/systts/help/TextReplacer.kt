package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.service.systts.help.exception.TextReplacerException

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
    fun replace(text: String, onReplaceError: (t: TextReplacerException) -> Unit): String {
        var s = text
        rules.forEach { rule ->
            kotlin.runCatching {
                s = if (rule.isRegex)
                    s.replace(Regex(rule.pattern), rule.replacement)
                else
                    s.replace(rule.pattern, rule.replacement)
            }.onFailure {
                onReplaceError.invoke(TextReplacerException(rule, it))
            }
        }

        return s
    }
}