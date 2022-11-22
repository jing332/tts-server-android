package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.SysTtsNumericalEditView
import com.github.jing332.tts_server_android.ui.custom.adapter.SysTtsConfigListItemAdapter
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.RESULT_ADD
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toastOnUi
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class TtsConfigFragment : Fragment(), SysTtsConfigListItemAdapter.CallBack {
    companion object {
        const val TAG = "TtsConfigFragment"
        const val ACTION_ON_CONFIG_CHANGED = "on_config_changed"
    }

    private val viewModel: TtsConfigFragmentViewModel by activityViewModels()
    private val binding: FragmentTtsConfigBinding by lazy {
        FragmentTtsConfigBinding.inflate(
            layoutInflater
        )
    }

    private val adapter: SysTtsConfigListItemAdapter by lazy {
        SysTtsConfigListItemAdapter()
    }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getSerializableExtra(KEY_DATA)
            data?.let {
                val data = data as SysTts
                if (result.resultCode == RESULT_ADD) appDb.sysTtsDao.insert(data)
                else appDb.sysTtsDao.update(data)
            }
        }

    fun startEditActivity() {
        val intent =
            Intent(requireContext(), TtsConfigEditActivity::class.java)
        startForResult.launch(intent)
    }

    /* 监听数据库变化 */
    private fun observeListData() {
        viewModel.viewModelScope.launch {
            appDb.sysTtsDao.flowAll().conflate().collect {
                Log.d(TAG, "setItems: $it")
                adapter.setItems(it)
                if (!viewModel.checkMultiVoiceFormat(it)) {
                    AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.msg_aside_and_dialogue_format_different))
                        .show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeListData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewList.let {
            it.layoutManager = layoutManager
            it.adapter = this@TtsConfigFragment.adapter
        }
        adapter.callBack = this
    }


    @SuppressLint("SetTextI18n")
    fun setAudioRequestTimeout() {
        val numPicker = NumberPicker(requireContext())
        numPicker.maxValue = 30
        numPicker.minValue = 2
        numPicker.value = 5
        val displayList = ArrayList<String>()
        for (i in 2..30) {
            displayList.add("${i}秒")
        }
        numPicker.displayedValues = displayList.toList().toTypedArray()

        numPicker.value = SysTtsConfig.requestTimeout / 1000 //转为秒
        AlertDialog.Builder(requireContext()).setTitle(R.string.set_audio_request_timeout)
            .setView(numPicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.requestTimeout = numPicker.value * 1000 //转为毫秒
                App.localBroadcast.sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
            }
            .show()
    }

    fun importConfig() {
        val et = EditText(requireContext())
        et.hint = getString(R.string.url_net)
        AlertDialog.Builder(requireContext()).setTitle(R.string.import_config).setView(et)
            .setPositiveButton(R.string.import_from_clip) { _, _ ->
                viewModel.viewModelScope.launch {
                    val err = viewModel.importConfig(ClipboardUtils.text.toString())
                    err?.let {
                        longToast("导入配置失败：$err")
                    }
                }
            }.setNegativeButton(getString(R.string.import_from_url)) { _, _ ->
                val err = viewModel.importConfigByUrl(et.text.toString())
                err?.let {
                    longToast("导入配置失败：$err")
                }
            }

            .show()
    }

    fun exportConfig() {
        val jsonStr = viewModel.exportConfig()
        val tv = TextView(requireContext())
        tv.setTextIsSelectable(true)
        tv.setPadding(50, 50, 50, 0)
        tv.text = jsonStr
        AlertDialog.Builder(requireContext()).setTitle(R.string.export_config).setView(tv)
            .setPositiveButton(R.string.copy) { _, _ ->
                ClipboardUtils.copyText(jsonStr)
                toastOnUi(R.string.copied)
            }.setNegativeButton("上传到URL") { _, _ ->
                viewModel.viewModelScope.launch {
                    val result = viewModel.uploadConfigToUrl(jsonStr)
                    if (result.isSuccess) {
                        ClipboardUtils.copyText(result.getOrNull())
                        longToast("已复制URL：\n${result.getOrNull()}")
                    }
                }
            }.show()
    }

    fun setMinDialogueLength() {
        val numList = arrayListOf("不限制")
        for (i in 1..10)
            numList.add("对话字数 ≥ $i")

        val picker = NumberPicker(requireContext()).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        AlertDialog.Builder(requireContext()).setTitle("对话文本最小匹配汉字数").setMessage("(包括中文符号)")
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.minDialogueLength = picker.value
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.minDialogueLength = 0
            }
            .show()
    }

    override fun onSwitchClick(view: View?, position: Int) {
        val checkBox = view as CheckBox
        // 检测是否开启多语音
        if (viewModel.onCheckBoxChanged(adapter.items, position, checkBox.isChecked)) {
            requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.warning)
                .setMessage(R.string.please_check_multi_voice_option).show()
            checkBox.isChecked = false
        }
    }

    override fun onContentClick(data: SysTts) {
        val editView = SysTtsNumericalEditView(requireContext())
        editView.setPadding(25, 25, 25, 50)
        data.msTtsProperty?.let { pro ->
            editView.setRate(pro.prosody.rate)
            editView.setVolume(pro.prosody.volume)
            editView.setStyleDegree(pro.expressAs?.styleDegree ?: 1F)
            editView.isStyleDegreeVisible = pro.api != TtsApiType.EDGE

            val dlg = AlertDialog.Builder(requireContext())
                .setTitle("数值调节").setView(editView)
                .setOnDismissListener {
                    data.msTtsProperty?.let {
                        it.prosody.rate = editView.rateValue
                        it.prosody.volume = editView.volumeValue
                        it.expressAs?.styleDegree = editView.styleDegreeValue
                    }
                    appDb.sysTtsDao.update(data)
                }.create()
            dlg.window?.setDimAmount(0.5F)
            dlg.show()
        }
    }

    override fun onEdit(data: SysTts) {
        val intent =
            Intent(requireContext(), TtsConfigEditActivity::class.java)
        intent.putExtra(KEY_DATA, data)
        startForResult.launch(intent)
    }

    override fun onDelete(data: SysTts) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.sysTtsDao.delete(data)
            }
            .show()
    }

    override fun onItemLongClick(view: View, data: SysTts): Boolean {
        /* 列表item的长按菜单 */
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_systts_list_item, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            var target = when (item.itemId) {
                R.id.menu_setAsDialogue -> {
                    ReadAloudTarget.DIALOGUE
                }
                R.id.menu_setAsAside -> {
                    ReadAloudTarget.ASIDE
                }
                else -> {
                    ReadAloudTarget.DEFAULT
                }
            }
            data.readAloudTarget = target
            appDb.sysTtsDao.update(data)
            App.localBroadcast.sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))

            false
        }
        popupMenu.show()

        return true
    }
}
