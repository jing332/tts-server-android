package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import java.util.regex.Pattern

class ReplaceRuleEditViewModel : ViewModel() {
    val liveData: MutableLiveData<ReplaceRule> by lazy { MutableLiveData() }

    fun load(data: ReplaceRule?) {
        liveData.value = data
            ?: ReplaceRule(
                name = "",
                pattern = "",
                replacement = ""
            )
    }

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