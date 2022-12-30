package com.github.jing332.tts_server_android.ui.systts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.drake.net.Net
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.LegadoHttpTts
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsConfigImportBottomSheetBinding
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import java.time.LocalDateTime


class ConfigImportBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ConfigImportBottomSheetFragment"

        const val TYPE_APP = 1
        const val TYPE_LEGADO = 2

        const val SRC_CLIPBOARD = 1
        const val SRC_URL = 2
    }

    val binding: SysttsConfigImportBottomSheetBinding by lazy {
        SysttsConfigImportBottomSheetBinding.inflate(layoutInflater)
    }

    private val type: Int
        get() {
            return when (binding.groupType.checkedButtonId) {
                binding.btnTypeApp.id -> TYPE_APP
                binding.btnTypeLegado.id -> TYPE_LEGADO
                else -> TYPE_APP
            }
        }

    private val src: Int
        get() {
            return when (binding.groupSource.checkedButtonId) {
                R.id.btn_src_clipboard -> SRC_CLIPBOARD
                R.id.btn_src_url -> SRC_URL
                else -> SRC_CLIPBOARD
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            groupType.check(R.id.btn_type_app)
            groupSource.check(R.id.btn_src_clipboard)

            groupSource.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (checkedId == R.id.btn_src_url)
                    tilUrl.isGone = !isChecked
            }

            btnImport.clickWithThrottle {
                lifecycleScope.launch {
                    waitDialog.show()
                    kotlin.runCatching {
                        importConfig(type, src, tilUrl.editText!!.text.toString())
                    }.onFailure {
                        it.printStackTrace()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.import_failed)
                            .setMessage(it.message.toString().ifEmpty { it.cause.toString() })
                            .show()
                    }
                    waitDialog.dismiss()
                }

            }
        }
    }

    private val waitDialog: WaitDialog by lazy { WaitDialog(requireContext()) }

    private suspend fun importConfig(type: Int, src: Int, url: String? = null) {
        val json = when (src) {
            SRC_URL -> {
                withIO { Net.get(url.toString()).execute() }
            }
            else -> ClipboardUtils.text.toString()
        }

        getImportList(json, type == TYPE_LEGADO)?.let { list ->
            val ttsList = mutableListOf<Pair<SystemTtsGroup, SystemTts>>()
            list.forEach {
                it.list.forEach { tts ->
                    ttsList.add(Pair(it.group, tts))
                }
            }
            // 显示item
            val items = ttsList.map { "${it.first.name}⤵️\n${it.second.displayName.toString()}" }
            // 全选item
            val checkedList = mutableListOf(*items.map { true }.toTypedArray()).toBooleanArray()
            // 已选择的tts
            val selectedList = mutableListOf(*ttsList.toTypedArray())

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.please_choose)
                .setMultiChoiceItems(
                    items.toTypedArray(),
                    checkedList
                ) { _, which, isChecked ->
                    val tts = ttsList[which]
                    selectedList.remove(tts)
                    if (isChecked)
                        selectedList.add(tts)
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val groupWithTtsList = mutableListOf<GroupWithTtsItem>()
                    // 查重 添加Group
                    selectedList.distinctBy { it.first.id }.forEach {
                        groupWithTtsList.add(GroupWithTtsItem(group = it.first, mutableListOf()))
                    }
                    // 添加到对应的Group
                    selectedList.forEach { pair ->
                        val group = groupWithTtsList.last { it.group.id == pair.first.id }
                        (group.list as MutableList).add(pair.second)
                    }

                    doImport(groupWithTtsList)
                }
                .show()
        }
    }


    private fun doImport(list: List<GroupWithTtsItem>) {
        list.forEach {
            appDb.systemTtsDao.insertGroup(it.group)
            appDb.systemTtsDao.insertTts(*it.list.toTypedArray())
        }
    }

    private fun getImportList(json: String, fromLegado: Boolean): List<GroupWithTtsItem>? {
        val groupName = App.context.getString(
            R.string.systts_from_legado_app_group_name,
            LocalDateTime.now()
        )
        val groupId = System.currentTimeMillis()
        if (fromLegado) {
            App.jsonBuilder.decodeFromString<List<LegadoHttpTts>>(json).ifEmpty { return null }
                .let { list ->
                    return listOf(GroupWithTtsItem(
                        group =
                        SystemTtsGroup(id = groupId, name = groupName),
                        list = list.map {
                            SystemTts(
                                groupId = groupId,
                                id = it.id,
                                displayName = it.name,
                                tts = HttpTTS(url = it.url, header = it.header)
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
                        group =
                        SystemTtsGroup(id = groupId, name = groupName),
                        list = list.map {
                            SystemTts(
                                displayName = it.displayName,
                                tts = it.tts
                            )
                        }
                    )
                )
            }
        }
    }

}