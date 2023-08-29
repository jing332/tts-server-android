package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

internal class ManagerViewModel : ViewModel() {
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

    fun updateSearchResult(text: String, type: SearchType) {
        if (list.value.isEmpty() || text.isBlank()) {
            _list.value = appDb.replaceRuleDao.allGroupWithReplaceRules()
            return
        }

        val resultList = mutableListOf<GroupWithReplaceRule>()
        list.value.forEach {
            val subList = mutableListOf<ReplaceRule>()
            val groupWithRules = GroupWithReplaceRule(it.group, subList)
            resultList.add(groupWithRules)

            it.list.forEach { rule ->
                when (type) {
                    SearchType.GROUP_NAME -> {
                        if (it.group.name.contains(text)) subList.add(rule)
                    }

                    SearchType.NAME -> {
                        if (rule.name.contains(text)) subList.add(rule)
                    }

                    SearchType.PATTERN -> {
                        if (rule.pattern.contains(text)) subList.add(rule)
                    }

                    SearchType.REPLACEMENT -> {
                        if (rule.replacement.contains(text)) subList.add(rule)
                    }
                }
            }

            if (subList.isEmpty())
                resultList.remove(groupWithRules)
        }

        _list.value = resultList
    }
}