package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class ReplaceRuleEditViewModel : ViewModel() {
    fun test(text: String, pattern: String, replacement: String, isRegex: Boolean): String {
        return if (isRegex) {
            return try {
                val regex = Pattern.compile(pattern)
                text.replace(regex.toRegex(), replacement)
            } catch (e: Exception) {
                e.message ?: e.cause?.message.toString()
            }
        } else {
            text.replace(pattern, replacement)
        }
    }
}