package com.github.jing332.tts_server_android.ui.systts.replace.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData

class ReplaceRuleEditActivityViewModel : ViewModel() {
    val liveData: MutableLiveData<ReplaceRuleItemData> by lazy { MutableLiveData() }

    fun load(data: ReplaceRuleItemData) {
        liveData.value = data
    }
}