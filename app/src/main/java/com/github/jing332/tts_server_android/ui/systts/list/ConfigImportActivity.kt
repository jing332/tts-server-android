package com.github.jing332.tts_server_android.ui.systts.list

import android.os.Bundle
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.LegadoHttpTts
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsListImportBinding
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.base.import1.BaseConfigImportActivity
import com.github.jing332.tts_server_android.ui.base.import1.ConfigImportItemModel
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.toJsonListString
import com.github.jing332.tts_server_android.util.toast
import kotlinx.serialization.decodeFromString

class ConfigImportActivity : BaseConfigImportActivity() {
    private val binding by lazy { SysttsListImportBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.groupType.check(binding.btnTypeApp.id)
    }

    private val isLegado: Boolean
        get() = binding.groupType.checkedButtonId == binding.btnTypeLegado.id


    @Suppress("UNCHECKED_CAST")
    override fun onImport(json: String) {
        val allList = mutableListOf<ConfigImportItemModel>()
        getImportList(json.toJsonListString(), isLegado)?.forEach { groupWithTts ->
            val group = groupWithTts.group
            groupWithTts.list.forEach { sysTts ->
                allList.add(
                    ConfigImportItemModel(
                        true, sysTts.displayName.toString(),
                        group.name, Pair(group, sysTts)
                    )
                )
            }
        }
        displayListSelectDialog(allList) { list ->
            list.map { it as Pair<SystemTtsGroup, SystemTts> }
                .forEach {
                    val group = it.first
                    val tts = it.second
                    appDb.systemTtsDao.insertGroup(group)
                    appDb.systemTtsDao.insertTts(tts)
                }
            finish()
            toast(getString(R.string.config_import_success_msg, list.size))
        }
    }

    private fun getImportList(json: String, fromLegado: Boolean): List<GroupWithTtsItem>? {
        val groupName = StringUtils.formattedDate()
        val groupId = System.currentTimeMillis()
        val groupCount = appDb.systemTtsDao.groupCount
        if (fromLegado) {
            App.jsonBuilder.decodeFromString<List<LegadoHttpTts>>(json).ifEmpty { return null }
                .let { list ->
                    return listOf(GroupWithTtsItem(
                        group =
                        SystemTtsGroup(id = groupId, name = groupName, order = groupCount),
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
                App.jsonBuilder.decodeFromString<List<GroupWithTtsItem>>(json)
            } else {
                val list = App.jsonBuilder.decodeFromString<List<CompatSystemTts>>(json)
                listOf(
                    GroupWithTtsItem(
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
}