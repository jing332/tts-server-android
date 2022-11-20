package com.github.jing332.tts_server_android.ui.systts.replace.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import java.util.regex.Pattern

class ReplaceRuleEditActivityViewModel : ViewModel() {
    val liveData: MutableLiveData<ReplaceRuleItemData> by lazy { MutableLiveData() }

    fun load(data: ReplaceRuleItemData?) {
        liveData.value = data
            ?: ReplaceRuleItemData(
                true,
                isRegex = false,
                name = "",
                pattern = "",
                replacement = ""
            )
    }

    fun test(text: String, pattern: String, replacement: String, isRegex: Boolean): String {
        return if (isRegex) {
            val regex = Pattern.compile(pattern)
            text.replace(regex.toRegex(), replacement)
        }else{
            text.replace(pattern, replacement)
        }
    }
}