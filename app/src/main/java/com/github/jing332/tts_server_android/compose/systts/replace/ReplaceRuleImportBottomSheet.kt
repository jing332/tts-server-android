package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.jing332.tts_server_android.compose.systts.ConfigImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.ConfigModel
import com.github.jing332.tts_server_android.compose.systts.SelectImportConfigDialog
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.utils.StringUtils
import com.github.jing332.tts_server_android.utils.toJsonListString

@Suppress("UNCHECKED_CAST")
@Composable
fun ReplaceRuleImportBottomSheet(onDismissRequest: () -> Unit) {
    var list by remember { mutableStateOf<List<ConfigModel>?>(null) }
    if (list != null) {
        SelectImportConfigDialog(
            onDismissRequest = { list = null },
            models = list!!,
            onSelectedList = { selectedList ->
                selectedList.map { it as Pair<ReplaceRuleGroup, ReplaceRule> }.forEach {
                    val group = it.first
                    val rule = it.second

                    appDb.replaceRuleDao.insert(rule)
                    appDb.replaceRuleDao.insertGroup(group)
                }
                appDb.replaceRuleDao.updateAllOrder()
                selectedList.size
            }
        )
    }

    ConfigImportBottomSheet(onDismissRequest = onDismissRequest, onImport = { json ->
        val allList = mutableListOf<ConfigModel>()
        if (json.contains("\"group\"")) {
            AppConst.jsonBuilder.decodeFromString<List<GroupWithReplaceRule>>(json.toJsonListString())
                .forEach { groupWithRule ->
                    val group = groupWithRule.group
                    groupWithRule.list.forEach {
                        allList.add(
                            ConfigModel(
                                isSelected = true,
                                title = it.name,
                                subtitle = group.name,
                                data = Pair(group, it)
                            )
                        )
                    }
                }

        } else {
            val groupName = StringUtils.formattedDate()
            val group = ReplaceRuleGroup(name = groupName)
            AppConst.jsonBuilder.decodeFromString<List<ReplaceRule>>(json.toJsonListString())
                .forEach {
                    allList.add(
                        ConfigModel(
                            isSelected = true, title = it.name, subtitle = groupName,
                            data = Pair(group, it.apply { groupId = group.id })
                        )
                    )
                }
        }

        list = allList
    })
}