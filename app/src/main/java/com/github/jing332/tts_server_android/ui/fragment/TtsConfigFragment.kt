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
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.ui.custom.adapter.SysTtsConfigListItemAdapter
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.KEY_DATA
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.KEY_POSITION
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toastOnUi


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
        recyclerAdapter.editButtonClick = this
        recyclerAdapter.delButtonClick = this
        recyclerAdapter.itemLongClick = this

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewList.apply {
            this.layoutManager = layoutManager
            this.adapter = recyclerAdapter
        }
        /* 监听整个列表数据list */
        viewModel.ttsCfg.observe(this) {
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
            else
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
                AlertDialog.Builder(requireContext()).setTitle("确认删除？")
                    .setPositiveButton("删除") { _, _ ->
                        Log.e(TAG, "remove item: $position")
                        recyclerAdapter.remove(position)
                    }
                    .show()
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

    fun importConfig() {
        val typeList = arrayOf("从网络地址导入(暂不支持)", "从剪贴板导入")
        AlertDialog.Builder(requireContext()).setTitle("导入配置")
            .setItems(typeList) { _, which ->
                when (which) {
                    0 -> {
                    }
                    1 -> {
                        val clipboard =
                            (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
                        val item =
                            if ((clipboard?.itemCount ?: -1) >= 0) clipboard?.getItemAt(0) else null
                        item?.let {
                            val err = viewModel.importConfig(it.text.toString())
                            err?.let {
                                longToast("导入配置失败：$err")
                            }
                        }
                    }
                }
            }.show()
    }

    fun exportConfig() {
        val jsonStr = viewModel.exportConfig()
        val tv = TextView(requireContext())
        tv.setTextIsSelectable(true)
        tv.setPadding(50, 50, 50, 0)
        tv.text = jsonStr
        AlertDialog.Builder(requireContext()).setTitle("导出配置").setView(tv)
            .setPositiveButton("复制配置") { _, _ ->
                val cm =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val mClipData = ClipData.newPlainText("config", jsonStr)
                cm?.setPrimaryClip(mClipData)
                toastOnUi(R.string.copied)
            }.show()
    }
}