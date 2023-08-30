package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule

class RuleEditViewModel : ViewModel() {
    fun doReplace(rule: ReplaceRule, text: String): String {
        return if (rule.isRegex)
            Regex(rule.pattern).replace(text, rule.replacement)
        else
            text.replace(rule.pattern, rule.replacement)
    }
}