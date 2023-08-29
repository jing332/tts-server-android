package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

internal class ManagerViewModel : ViewModel() {
    var listItemIndex = 0
    var listItemOffset = 0

    private val _list = MutableStateFlow<List<GroupWithReplaceRule>>(emptyList())
    val list: MutableStateFlow<List<GroupWithReplaceRule>>
        get() = _list

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appDb.replaceRuleDao.updateAllOrder()
            appDb.replaceRuleDao.flowAllGroupWithReplaceRules().conflate().collectLatest {
                _list.value = it
            }
        }
    }
}