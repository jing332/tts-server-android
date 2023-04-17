package com.github.jing332.tts_server_android.ui.systts.list

import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.LegadoHttpTts
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsListImportBinding
import com.github.jing332.tts_server_android.model.speech.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.speech.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.base.import1.BaseImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.base.import1.ConfigItemModel
import com.github.jing332.tts_server_android.utils.StringUtils
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toJsonListString
import kotlinx.serialization.decodeFromString

class ImportConfigBottomSheetFragment : BaseImportConfigBottomSheetFragment() {
    companion object {
        const val TAG = "ConfigImportBottomSheetFragment"
    }

    private val binding by lazy { SysttsListImportBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTopContentView(binding.root)
        binding.groupType.check(binding.btnTypeApp.id)
    }

    private val isLegado: Boolean
        get() = binding.groupType.checkedButtonId == binding.btnTypeLegado.id

    override fun checkJson(json: String) = json.contains("tts")

    @Suppress("UNCHECKED_CAST")
    override fun onImport(json: String) {
        val allList = mutableListOf<ConfigItemModel>()
        getImportList(json.toJsonListString(), isLegado)?.forEach { groupWithTts ->
            val group = groupWithTts.group
            groupWithTts.list.forEach { sysTts ->
                allList.add(
                    ConfigItemModel(
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

            longToast(getString(R.string.config_import_success_msg, list.size))
        }
    }

    private fun getImportList(json: String, fromLegado: Boolean): List<GroupWithSystemTts>? {
        val groupName = StringUtils.formattedDate()
        val groupId = System.currentTimeMillis()
        val groupCount = appDb.systemTtsDao.groupCount
        if (fromLegado) {
            AppConst.jsonBuilder.decodeFromString<List<LegadoHttpTts>>(json).ifEmpty { return null }
                .let { list ->
                    return listOf(GroupWithSystemTts(
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


}