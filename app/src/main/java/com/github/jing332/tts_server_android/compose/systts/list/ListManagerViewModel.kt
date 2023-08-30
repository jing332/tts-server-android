package com.github.jing332.tts_server_android.compose.systts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class ListManagerViewModel : ViewModel() {
    var listItemIndex: Int = 0
    var listItemOffset: Int = 0

    private val _list = MutableStateFlow<List<GroupWithSystemTts>>(emptyList())
    val list: StateFlow<List<GroupWithSystemTts>> get() = _list

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appDb.systemTtsDao.updateAllOrder()
            appDb.systemTtsDao.getFlowAllGroupWithTts().conflate().collectLatest {
                _list.value = it
            }
        }
    }

    fun updateTtsEnabled(item: SystemTts, enabled: Boolean) {
        if (!SystemTtsConfig.isVoiceMultipleEnabled.value && enabled)
            appDb.systemTtsDao.allEnabledTts.forEach { systts ->
                if (systts.speechRule.target == SpeechTarget.BGM || systts.speechRule.isStandby)
                    return@forEach

                if (systts.speechRule.target == item.speechRule.target) {
                    if (systts.speechRule.tagRuleId == item.speechRule.tagRuleId
                        && systts.speechRule.tag == item.speechRule.tag
                        && systts.speechRule.tagName == item.speechRule.tagName
                    )
                        appDb.systemTtsDao.updateTts(systts.copy(isEnabled = false))
                }
            }

        appDb.systemTtsDao.updateTts(item.copy(isEnabled = enabled))
    }

    fun updateGroupEnable(
        item: GroupWithSystemTts,
        enabled: Boolean
    ) {
        if (!SystemTtsConfig.isGroupMultipleEnabled.value && enabled) {
            list.value.forEach {
                it.list.forEach { systts ->
                    if (systts.isEnabled)
                        appDb.systemTtsDao.updateTts(systts.copy(isEnabled = false))
                }
            }
        }

        appDb.systemTtsDao.updateTts(
            *item.list.filter { it.isEnabled != enabled }.map { it.copy(isEnabled = enabled) }
                .toTypedArray()
        )
    }
}