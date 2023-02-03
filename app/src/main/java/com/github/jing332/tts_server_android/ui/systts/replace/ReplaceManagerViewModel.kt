package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.util.toJsonListString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib
import java.util.*

class ReplaceManagerViewModel : ViewModel() {
    fun configToJson(): String {
        val list = appDb.replaceRuleDao.allGroupWithReplaceRules()
        return App.jsonBuilder.encodeToString(list)
    }

    @Suppress("UNCHECKED_CAST")
    fun exportGroup(model: ReplaceRuleGroupModel): String {
        return App.jsonBuilder.encodeToString(
            GroupWithReplaceRule(
                model.data, (model.itemSublist as List<ReplaceRuleModel>).map { it.data }
            )
        )
    }

    fun importConfig(json: String): String? {
        try {
            if (json.contains("\"group\"")) {
                App.jsonBuilder.decodeFromString<List<GroupWithReplaceRule>>(json.toJsonListString())
                    .forEach {
                        appDb.replaceRuleDao.insert(*it.list.toTypedArray())
                        appDb.replaceRuleDao.insertGroup(it.group)
                    }
            } else {
                App.jsonBuilder.decodeFromString<List<ReplaceRule>>(json.toJsonListString())
                    .forEach { appDb.replaceRuleDao.insert(it) }
            }
        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    fun importConfigFromUrl(url: String): String? {
        return try {
            importConfig(Tts_server_lib.httpGet(url, "").decodeToString())
        } catch (e: Exception) {
            e.message
        }
    }
}