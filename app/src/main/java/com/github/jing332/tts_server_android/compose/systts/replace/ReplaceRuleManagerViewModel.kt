package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

internal class ReplaceRuleManagerViewModel : ViewModel() {
    private var allList = listOf<GroupWithReplaceRule>()

    private val _list = MutableStateFlow<List<GroupWithReplaceRule>>(emptyList())
    val list: MutableStateFlow<List<GroupWithReplaceRule>>
        get() = _list

    var searchType by mutableStateOf(SearchType.NAME)
    var searchText by mutableStateOf("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appDb.replaceRuleDao.getGroup(DEFAULT_GROUP_ID) ?: run {
                appDb.replaceRuleDao.insertGroup(
                    ReplaceRuleGroup(
                        DEFAULT_GROUP_ID,
                        app.getString(R.string.default_group)
                    )
                )
            }

            appDb.replaceRuleDao.updateAllOrder()
            appDb.replaceRuleDao.flowAllGroupWithReplaceRules().collectLatest {
                allList = it
                updateSearchResult()
            }
        }
    }


    fun updateSearchResult(
        text: String = searchText,
        type: SearchType = searchType,
        src: List<GroupWithReplaceRule> = allList
    ) {
        if (src.isEmpty() || text.isBlank()) {
            _list.value = src
            return
        }

        val resultList = mutableListOf<GroupWithReplaceRule>()
        src.forEach {
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

    fun moveTop(rule: ReplaceRule) {
        appDb.replaceRuleDao.update(rule.copy(order = 0))
    }

    fun moveBottom(rule: ReplaceRule) {
        appDb.replaceRuleDao.update(rule.copy(order = appDb.replaceRuleDao.count))
    }

    fun deleteRule(rule: ReplaceRule) {
        appDb.replaceRuleDao.delete(rule)
    }

    fun deleteGroup(groupWithRules: GroupWithReplaceRule) {
        appDb.replaceRuleDao.delete(*groupWithRules.list.toTypedArray())
        appDb.replaceRuleDao.deleteGroup(groupWithRules.group)
    }
}