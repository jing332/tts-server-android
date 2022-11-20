package com.github.jing332.tts_server_android.ui.systts.replace

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import com.github.jing332.tts_server_android.databinding.ActivityReplaceRuleBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.systts.replace.edit.ReplaceRuleEditActivity
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.longToastOnUi
import com.github.jing332.tts_server_android.util.toastOnUi

@Suppress("DEPRECATION")
class ReplaceManagerActivity : BackActivity(), ReplaceRuleItemAdapter.CallBack {
    companion object {
        const val TAG = "ReplaceRuleActivity"
        const val KEY_SWITCH = "switch"
    }

    private val viewModel: ReplaceManagerViewModel by viewModels()
    private val binding by lazy { ActivityReplaceRuleBinding.inflate(layoutInflater) }
    private val adapter by lazy { ReplaceRuleItemAdapter(arrayListOf()) }

    private var isReplaceEnabled = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isReplaceEnabled = intent?.getBooleanExtra(KEY_SWITCH, false) ?: false

        adapter.callBack = this
        binding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        viewModel.listLiveData.observe(this) { data ->
            adapter.itemList.clear()
            data.forEach {
                adapter.append(it)
            }
        }

        viewModel.load()
    }

    private val startForResult = registerForActivityResult(StartActivityForResult()) { result ->
        result.data?.apply {
            val position = getIntExtra(KeyConst.KEY_POSITION, -1)
            val data = getSerializableExtra(KeyConst.KEY_DATA) as ReplaceRuleItemData
            if (position < 0) {
                adapter.append(data)
                viewModel.listLiveData.value?.add(data)
            } else {
                adapter.itemList[position] = data
                adapter.notifyItemChanged(position)
                viewModel.listLiveData.value!![position] = data
            }
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.save()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_replace_manager, menu)
        /* 开关选项 */
        val item = menu?.findItem(R.id.app_bar_switch)
        item!!.apply {
            val switch = actionView?.findViewById<SwitchCompat>(R.id.menu_switch)
            switch!!.let {
                it.isChecked = isReplaceEnabled

                it.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra(KEY_SWITCH, switch.isChecked)
                    this@ReplaceManagerActivity.setResult(RESULT_OK, intent)
                }
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                val intent = Intent(this, ReplaceRuleEditActivity::class.java)
                intent.putExtra(KeyConst.KEY_POSITION, -1)
                startForResult.launch(intent)
            }
            R.id.menu_importConfig -> {
                val et = EditText(this)
                et.setHint(R.string.url_net)
                AlertDialog.Builder(this).setView(et)
                    .setPositiveButton(R.string.import_from_clip) { _, _ ->
                        val err = viewModel.importConfig(ClipboardUtils.text.toString())
                        err?.let {
                            longToastOnUi("导入失败：$it")
                        }
                    }.setNegativeButton(R.string.import_from_url) { _, _ ->
                        val err = viewModel.importConfigFromUrl(et.text.toString())
                        err?.let {
                            longToastOnUi("导入失败：$it")
                        }
                    }.show()
            }

            R.id.menu_exportConfig -> {
                val jsonStr = viewModel.exportConfig()
                val tv = TextView(this)
                tv.text = jsonStr
                AlertDialog.Builder(this).setView(tv).setPositiveButton(R.string.copy) { _, _ ->
                    ClipboardUtils.copyText(jsonStr)
                    toastOnUi(R.string.copied)
                }.setNegativeButton("上传到URL") { _, _ ->
                    val ret = viewModel.uploadConfigToUrl(jsonStr)
                    if (ret.isSuccess) {
                        ClipboardUtils.copyText(ret.getOrNull())
                        longToastOnUi("已复制URL：\n${ret.getOrNull()}")
                    } else {
                        longToastOnUi("上传失败：${ret.exceptionOrNull()?.message}")
                    }
                }
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun switch(position: Int, isSelected: Boolean) {
        viewModel.switchChanged(position, isSelected)
    }

    override fun edit(position: Int) {
        val intent = Intent(this, ReplaceRuleEditActivity::class.java)
        intent.putExtra(KeyConst.KEY_POSITION, position)
        intent.putExtra(KeyConst.KEY_DATA, viewModel.listLiveData.value?.get(position))
        startForResult.launch(intent)
    }

    override fun delete(position: Int) {
        AlertDialog.Builder(this).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                adapter.remove(position)
                viewModel.list().removeAt(position)
            }.show()
    }

}