package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.jing332.tts_server_android.bean.LegadoHttpTts
import com.github.jing332.tts_server_android.compose.systts.ConfigImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.ConfigModel
import com.github.jing332.tts_server_android.compose.systts.SelectImportConfigDialog
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.model.speech.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.speech.tts.HttpTTS
import com.github.jing332.tts_server_android.utils.StringUtils

@Composable
fun ListImportBottomSheet(onDismissRequest: () -> Unit) {
    var selectDialog by remember { mutableStateOf<List<ConfigModel>?>(null) }
    if (selectDialog != null) {
        SelectImportConfigDialog(
            onDismissRequest = { selectDialog = null },
            models = selectDialog!!,
            onSelectedList = { list ->
                list.map {
                    @Suppress("UNCHECKED_CAST")
                    it as Pair<SystemTtsGroup, SystemTts>
                }
                    .forEach {
                        val group = it.first
                        val tts = it.second
                        appDb.systemTtsDao.insertGroup(group)
                        appDb.systemTtsDao.insertTts(tts)
                    }

                list.size
            }
        )
    }

    ConfigImportBottomSheet(onDismissRequest = onDismissRequest,
        onImport = { json ->
            val allList = mutableListOf<ConfigModel>()
            getImportList(json, false)?.forEach { groupWithTts ->
                val group = groupWithTts.group
                groupWithTts.list.forEach { sysTts ->
                    allList.add(
                        ConfigModel(
                            true, sysTts.displayName.toString(),
                            group.name, group to sysTts
                        )
                    )
                }
            }
            selectDialog = allList
        }
    )
}

private fun getImportList(json: String, fromLegado: Boolean): List<GroupWithSystemTts>? {
    val groupName = StringUtils.formattedDate()
    val groupId = System.currentTimeMillis()
    val groupCount = appDb.systemTtsDao.groupCount
    if (fromLegado) {
        AppConst.jsonBuilder.decodeFromString<List<LegadoHttpTts>>(json).ifEmpty { return null }
            .let { list ->
                return listOf(GroupWithSystemTts(
                    group = SystemTtsGroup(
                        id = groupId,
                        name = groupName,
                        order = groupCount
                    ),
                    list = list.map {
                        SystemTts(
                            groupId = groupId,
                            id = it.id,
                            displayName = it.name,
                            tts = HttpTTS(
                                url = it.url,
                                header = it.header,
                                audioFormat = BaseAudioFormat(isNeedDecode = true)
                            )
                        )
                    }

                ))
            }

    } else {
        return if (json.contains("\"group\"")) { // 新版数据结构
            AppConst.jsonBuilder.decodeFromString<List<GroupWithSystemTts>>(json)
        } else {
            val list = AppConst.jsonBuilder.decodeFromString<List<CompatSystemTts>>(json)
            listOf(
                GroupWithSystemTts(
                    group = appDb.systemTtsDao.getGroup()!!,
                    list = list.mapIndexed { index, value ->
                        SystemTts(
                            id = System.currentTimeMillis() + index,
                            displayName = value.displayName,
                            tts = value.tts
                        )
                    }
                )
            )
        }
    }
}