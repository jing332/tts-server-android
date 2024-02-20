package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.service.systts.help.exception.TextReplacerException

class TextReplacer {
    companion object {
        const val TAG = "ReplaceHelper"
    }

    private var map: MutableMap<Int, MutableList<ReplaceRule>> = mutableMapOf()

    fun load() {
        map.clear()
        appDb.replaceRuleDao.allGroupWithReplaceRules().forEach { groupWithRules ->
            if (map[groupWithRules.group.onExecution] == null)
                map[groupWithRules.group.onExecution] = mutableListOf()

            map[groupWithRules.group.onExecution]?.addAll(
                groupWithRules.list.filter { it.isEnabled }
            )
        }
    }

    /**
     * 执行替换
     */
    fun replace(
        text: String,
        onExecution: Int,
        onReplaceError: (t: TextReplacerException) -> Unit
    ): String {
        var s = text
        map[onExecution]?.forEach { rule ->
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