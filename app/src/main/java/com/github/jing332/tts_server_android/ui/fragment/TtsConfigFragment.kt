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
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_POSITION
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.ui.custom.SysTtsNumericalEditView
import com.github.jing332.tts_server_android.ui.custom.adapter.SysTtsConfigListItemAdapter
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toastOnUi
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class TtsConfigFragment : Fragment(), SysTtsConfigListItemAdapter.ClickListen,
    SysTtsConfigListItemAdapter.LongClickListen {
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

    private val recyclerAdapter: SysTtsConfigListItemAdapter by lazy {
        SysTtsConfigListItemAdapter(
            viewModel, arrayListOf()
        )
    }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getSerializableExtra(KEY_DATA)
            val position = result.data?.getIntExtra(KEY_POSITION, -1) ?: -1
            data?.let {
                val cfgItem = data as SysTtsConfigItem
                viewModel.onEditActivityResult(cfgItem, position)
            }
        }

    fun startEditActivity() {
        val intent =
            Intent(requireContext(), TtsConfigEditActivity::class.java)
        startForResult.launch(intent)
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

        recyclerAdapter.switchClick = this
        recyclerAdapter.contentClick = this
        recyclerAdapter.editButtonClick = this
        recyclerAdapter.delButtonClick = this
        recyclerAdapter.itemLongClick = this

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewList.apply {
            this.layoutManager = layoutManager
            this.adapter = recyclerAdapter
        }
        /* 监听整个列表数据list */
        viewModel.ttsCfgLiveData.observe(this) {
            Log.d(TAG, "item list changed: $it")
            recyclerAdapter.removeAll()
            it.list.forEach { item ->
                recyclerAdapter.append(item, false)
            }
        }
        /* 添加列表数据 */
        viewModel.appendItemDataLiveData.observe(this) {
            Log.d(TAG, "add item: $it")
            recyclerAdapter.append(it, true)
            if (!viewModel.checkMultiVoiceFormat()) {
                AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.msg_aside_and_dialogue_format_different))
                    .show()
            }
        }
        /* 替换列表数据 */
        viewModel.replacedItemDataLiveData.observe(this) {
            Log.d(TAG, "replace item: $it")
            recyclerAdapter.update(it.sysTtsConfigItem, it.position, it.updateUi)
            viewModel.saveData()
            if (!viewModel.checkMultiVoiceFormat())
                AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.msg_aside_and_dialogue_format_different))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
            requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        }
        /* 从本地加载数据 */
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        /* 保存到本地 */
        viewModel.saveData()
    }

    override fun onClick(view: View, position: Int) {
        when (view.id) {
            R.id.checkBox_switch -> {
                val checkBox = view as CheckBox
                /* 检测是否开启多语音 */
                if (viewModel.onCheckBoxChanged(position, checkBox.isChecked)) {
                    viewModel.saveData()
                    requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.warning)
                        .setMessage(R.string.please_check_multi_voice_option).show()
                    checkBox.isChecked = false
                }
            }
            R.id.btn_edit -> {
                val itemData = recyclerAdapter.itemList[position]
                val intent =
                    Intent(requireContext(), TtsConfigEditActivity::class.java)
                intent.putExtra(KEY_DATA, itemData)
                intent.putExtra(KEY_POSITION, position)
                startForResult.launch(intent)
            }
            R.id.btn_delete -> {
                AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        Log.e(TAG, "remove item: $position")
                        recyclerAdapter.remove(position)
                    }
                    .show()
            }
            R.id.tv_content -> {
                val editView = SysTtsNumericalEditView(requireContext())
                editView.setPadding(25, 25, 25, 50)
                viewModel.ttsCfgLiveData.value?.list?.get(position)?.let { itemData ->
                    itemData.voiceProperty.let { pro ->
                        editView.setRate(pro.prosody.rate)
                        editView.setVolume(pro.prosody.volume)
                        editView.setStyleDegree(pro.expressAs?.styleDegree ?: 1F)
                        editView.isStyleDegreeVisible = pro.api != TtsApiType.EDGE
                    }
                    val dlg = AlertDialog.Builder(requireContext())
                        .setTitle("数值调节").setView(editView)
                        .setOnDismissListener {
                            itemData.voiceProperty.let {
                                it.prosody.rate = editView.rateValue
                                it.prosody.volume = editView.volumeValue
                                it.expressAs?.styleDegree = editView.styleDegreeValue
                            }
                            viewModel.onEditActivityResult(itemData, position)
                        }.create()
                    dlg.window?.setDimAmount(0.5F)
                    dlg.show()
                }
            }
        }
    }

    override fun onLongClick(view: View, position: Int): Boolean {
        /* 列表item的长按菜单 */
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_systts_list_item, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_global -> { /* 全局 */
                    viewModel.onReadAloudTargetChanged(position, ReadAloudTarget.DEFAULT)
                }
                R.id.menu_setAsDialogue -> { /* 对话  */
                    viewModel.onReadAloudTargetChanged(position, ReadAloudTarget.DIALOGUE)
                }
                R.id.menu_setAsAside -> { /* 旁白 */
                    viewModel.onReadAloudTargetChanged(position, ReadAloudTarget.ASIDE)
                }
            }

            false
        }
        popupMenu.show()

        return true
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

        numPicker.value = viewModel.audioRequestTimeout / 1000 //转为秒
        AlertDialog.Builder(requireContext()).setTitle(R.string.set_audio_request_timeout)
            .setView(numPicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.audioRequestTimeout = numPicker.value * 1000 //转为毫秒
                App.localBroadcast.sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                viewModel.audioRequestTimeout = 5000
            }
            .show()
    }

    fun importConfig() {
        val et = EditText(requireContext())
        et.hint = "URL网络链接"
        AlertDialog.Builder(requireContext()).setTitle(R.string.import_config).setView(et)
            .setPositiveButton("从剪贴板导入") { _, _ ->
                viewModel.viewModelScope.launch {
                    val err = viewModel.importConfig(ClipboardUtils.text.toString())
                    err?.let {
                        longToast("导入配置失败：$err")
                    }
                }
            }.setNegativeButton("从上方URL导入") { _, _ ->
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
}