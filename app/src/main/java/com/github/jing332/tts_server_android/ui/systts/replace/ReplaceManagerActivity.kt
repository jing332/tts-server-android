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
import androidx.lifecycle.viewModelScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.databinding.ActivityReplaceRuleBinding
import com.github.jing332.tts_server_android.databinding.ItemReplaceRuleBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.systts.replace.edit.ReplaceRuleEditActivity
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.flow.conflate

@Suppress("DEPRECATION")
class ReplaceManagerActivity : BackActivity() {
    companion object {
        const val TAG = "ReplaceRuleActivity"
    }

    private val viewModel: ReplaceManagerViewModel by viewModels()
    private val binding by lazy { ActivityReplaceRuleBinding.inflate(layoutInflater) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val brv = binding.recyclerView.linear().setup {
            addType<ReplaceRule>(R.layout.item_replace_rule)
            onCreate {
                val binding = getBinding<ItemReplaceRuleBinding>()
                binding.btnEdit.setOnClickListener { edit(getModel()) }
                binding.btnDelete.setOnClickListener { delete(getModel()) }
                binding.checkBox.setOnClickListener {
                    appDb.replaceRuleDao.update(getModel())
                }
            }
            onBind { }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as ReplaceRule).id == (newItem as ReplaceRule).id
                }
            }
        }


        viewModel.viewModelScope.runOnIO {
            appDb.replaceRuleDao.flowAll().conflate().collect {
                runOnUI {
                    if (brv.models == null)
                        brv.models = it
                    else brv.setDifferModels(it)
                }
            }
        }
    }

    fun delete(data: ReplaceRule) {
        AlertDialog.Builder(this).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.replaceRuleDao.delete(data)
            }.show()
    }

    fun edit(data: ReplaceRule) {
        val intent = Intent(this, ReplaceRuleEditActivity::class.java)
        intent.putExtra(KeyConst.KEY_DATA, data)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(StartActivityForResult()) { result ->
        result.data?.apply {
            val data = getSerializableExtra(KeyConst.KEY_DATA) as ReplaceRule
            data.let {
                appDb.replaceRuleDao.insert(data)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_replace_manager, menu)
        /* 开关选项 */
        val item = menu?.findItem(R.id.app_bar_switch)
        item?.apply {
            val switch = actionView?.findViewById<SwitchCompat>(R.id.menu_switch)
            switch!!.let {
                it.isChecked = SysTtsConfig.isReplaceEnabled
                it.setOnClickListener {
                    SysTtsConfig.isReplaceEnabled = switch.isChecked
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



}