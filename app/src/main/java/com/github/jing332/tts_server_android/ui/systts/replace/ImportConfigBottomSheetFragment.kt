package com.github.jing332.tts_server_android.ui.systts.replace

import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.ConfigType
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.base.import1.ConfigItemModel
import com.github.jing332.tts_server_android.utils.StringUtils
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toJsonListString
import kotlinx.serialization.decodeFromString

class ImportConfigBottomSheetFragment :
    BaseImportConfigBottomSheetFragment(R.string.import_replace_rule, ConfigType.REPLACE_RULE) {
    companion object {
        const val TAG = "ImportConfigBottomSheetFragment"
    }

    @Suppress("UNCHECKED_CAST")
    override fun onImport(json: String) {
        val allList = mutableListOf<ConfigItemModel>()
        if (json.contains("\"group\"")) {
            AppConst.jsonBuilder.decodeFromString<List<GroupWithReplaceRule>>(json.toJsonListString())
                .forEach { groupWithRule ->
                    val group = groupWithRule.group
                    groupWithRule.list.forEach {
                        allList.add(
                            ConfigItemModel(
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
            AppConst.jsonBuilder.decodeFromString<List<ReplaceRule>>(json.toJsonListString())
                .forEach {
                    allList.add(
                        ConfigItemModel(
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
            longToast(getString(R.string.config_import_success_msg, list.size))
            dismiss()
        }
    }
}