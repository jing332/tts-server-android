package com.github.jing332.tts_server_android.ui.systts.replace

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.ui.base.import1.BaseConfigImportActivity
import com.github.jing332.tts_server_android.ui.base.import1.ConfigImportItemModel
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.toJsonListString
import com.github.jing332.tts_server_android.util.toast
import kotlinx.serialization.decodeFromString

class ConfigImportActivity : BaseConfigImportActivity() {
    @Suppress("UNCHECKED_CAST")
    override fun onImport(json: String) {
        val allList = mutableListOf<ConfigImportItemModel>()
        if (json.contains("\"group\"")) {
            App.jsonBuilder.decodeFromString<List<GroupWithReplaceRule>>(json.toJsonListString())
                .forEach { groupWithRule ->
                    val group = groupWithRule.group
                    groupWithRule.list.forEach {
                        allList.add(
                            ConfigImportItemModel(
                                isEnabled = true,
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
            App.jsonBuilder.decodeFromString<List<ReplaceRule>>(json.toJsonListString()).forEach {
                allList.add(
                    ConfigImportItemModel(
                        isEnabled = true, title = it.name, subtitle = groupName,
                        data = Pair(group, it.apply { groupId = group.id })
                    )
                )
            }
        }
        displayListSelectDialog(allList) { list ->
            list.map { it as Pair<ReplaceRuleGroup, ReplaceRule> }.forEach {
                val group = it.first
                val rule = it.second

                appDb.replaceRuleDao.insert(rule)
                appDb.replaceRuleDao.insertGroup(group)
            }
            toast(getString(R.string.config_import_success_msg, list.size))
            finish()
        }
    }
}