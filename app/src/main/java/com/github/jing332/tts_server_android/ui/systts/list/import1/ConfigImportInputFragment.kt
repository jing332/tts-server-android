package com.github.jing332.tts_server_android.ui.systts.list.import1

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.LegadoHttpTts
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsConfigImportInputFragmentBinding
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.text.SimpleDateFormat
import java.util.*

class ConfigImportInputFragment : Fragment() {
    companion object {
        const val TYPE_APP = 1
        const val TYPE_LEGADO = 2

        const val SRC_CLIPBOARD = 1
        const val SRC_FILE = 2
        const val SRC_URL = 3
    }

    val binding: SysttsConfigImportInputFragmentBinding by lazy {
        SysttsConfigImportInputFragmentBinding.inflate(layoutInflater, null, false)
    }

    private val vm: ConfigImportSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

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
                R.id.btn_src_local_file -> SRC_FILE
                R.id.btn_src_url -> SRC_URL
                else -> SRC_CLIPBOARD
            }
        }

    private val activityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            waitDialog.dismiss()
            waitDialog.setCancelable(true)
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.let { uri ->
                    binding.tilFilePath.editText!!.setText(uri.toString())

                }
            }
        }

    private fun filePicker() {
        waitDialog.show()
        waitDialog.setCancelable(false)
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            activityForResult.launch(Intent.createChooser(this, "Choose a file"))
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            groupType.check(R.id.btn_type_app)
            groupSource.check(R.id.btn_src_clipboard)

            groupSource.addOnButtonCheckedListener { _, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_src_url -> tilUrl.isGone = !isChecked
                    R.id.btn_src_local_file -> tilFilePath.isGone = !isChecked
                }
            }

            tilFilePath.setEndIconOnClickListener {
                filePicker()
            }

            btnImport.clickWithThrottle {
                lifecycleScope.launch {
                    waitDialog.show()
                    kotlin.runCatching {
                        val list = importConfig(
                            type, src,
                            tilUrl.editText!!.text.toString(),
                            tilFilePath.editText!!.text.toString()
                        )
                        vm.setPreview(list!!)
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

    private suspend fun importConfig(
        type: Int,
        src: Int,
        url: String? = null,
        fileUri: String? = null
    ): List<GroupWithTtsItem>? {
        val json = when (src) {
            SRC_URL -> withContext(Dispatchers.IO) { Net.get(url.toString()).execute() }
            SRC_FILE -> withContext(Dispatchers.IO) {
                val input = requireContext().contentResolver.openInputStream(Uri.parse(fileUri))
                val str = input!!.readBytes().decodeToString()
                input.close()
                return@withContext str
            }
            else -> ClipboardUtils.text.toString()
        }
            .run { if (!startsWith("[")) "[$this" else this }
            .run { if (!endsWith("]")) "$this]" else this }

        return getImportList(json, type == TYPE_LEGADO)

    }

    private fun getImportList(json: String, fromLegado: Boolean): List<GroupWithTtsItem>? {
        val groupName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
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
                        group = appDb.systemTtsDao.getGroupById(SystemTtsGroup.DEFAULT_GROUP_ID)!!,
                        list = list.mapIndexed { index, value ->
                            SystemTts(
                                id = System.currentTimeMillis() + index,
                                displayName = value.displayName,
                                groupId = SystemTtsGroup.DEFAULT_GROUP_ID,
                                tts = value.tts
                            )
                        }
                    )
                )
            }
        }
    }
}