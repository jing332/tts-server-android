package com.github.jing332.tts_server_android.compose.systts.list

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.vector.DefaultGroupName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import java.util.Collections

class ListManagerViewModel : ViewModel() {
    companion object {
        const val TAG = "ListManagerViewModel"
    }

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
                        && systts.speechRule.isStandby == item.speechRule.isStandby
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

    fun reorder(from: ItemPosition, to: ItemPosition) {
        if (from.key is String && to.key is String) {
            val fromKey = from.key as String
            val toKey = to.key as String

            if (fromKey.startsWith("g") && toKey.startsWith("g")) {
                val mList = list.value.map { it.group }.toMutableList()

                val fromId = fromKey.substring(2).toLong()
                val fromIndex = mList.indexOfFirst { it.id == fromId }

                val toId = toKey.substring(2).toLong()
                val toIndex = mList.indexOfFirst { it.id == toId }

                try {
                    Collections.swap(mList, fromIndex, toIndex)
                } catch (_: IndexOutOfBoundsException) {
                    return
                }
                mList.forEachIndexed { index, systemTtsGroup ->
                    if (systemTtsGroup.order != index)
                        appDb.systemTtsDao.updateGroup(systemTtsGroup.copy(order = index))
                }
            } else if (!fromKey.startsWith("g") && !toKey.startsWith("g")) {
                val (fromGId, fromId) = fromKey.split("_").map { it.toLong() }
                val (toGId, toId) = toKey.split("_").map { it.toLong() }
                if (fromGId != toGId) return

                val listInGroup = findListInGroup(fromGId).toMutableList()
                val fromIndex = listInGroup.indexOfFirst { it.id == fromId }
                val toIndex = listInGroup.indexOfFirst { it.id == toId }
                Log.d(TAG, "fromIndex: $fromIndex, toIndex: $toIndex")

                try {
                    Collections.swap(listInGroup, fromIndex, toIndex)
                } catch (_: IndexOutOfBoundsException) {
                    return
                }

                listInGroup.forEachIndexed { index, systts ->
                    Log.d(TAG, "$index ${systts.displayName}")
                    if (systts.order != index)
                        appDb.systemTtsDao.updateTts(systts.copy(order = index))
                }
            }

        }
    }

    private fun findListInGroup(groupId: Long): List<SystemTts> {
        return list.value.find { it.group.id == groupId }?.list?.sortedBy { it.order }
            ?: emptyList()
    }

    fun checkListData(context: Context) {
        appDb.systemTtsDao.getGroup(DEFAULT_GROUP_ID) ?: kotlin.run {
            appDb.systemTtsDao.insertGroup(
                SystemTtsGroup(
                    DEFAULT_GROUP_ID,
                    context.getString(R.string.default_group),
                    appDb.systemTtsDao.groupCount
                )
            )
        }

        if (appDb.systemTtsDao.ttsCount == 0)
            importDefaultListData(context)
    }

    private fun importDefaultListData(context: Context) {
        val json = context.assets.open("defaultData/list.json").readAllText()
        val list = AppConst.jsonBuilder.decodeFromString<List<GroupWithSystemTts>>(json)
        viewModelScope.launch(Dispatchers.IO) {
            appDb.systemTtsDao.insertGroupWithTts(*list.toTypedArray())
        }
    }

}