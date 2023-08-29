package com.github.jing332.tts_server_android.compose.systts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
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
}