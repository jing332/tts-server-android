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
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.databinding.SysttsReplaceActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsReplaceRuleItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ReplaceManagerActivity : BackActivity() {
    companion object {
        const val TAG = "ReplaceRuleActivity"
    }

    private val vm: ReplaceManagerViewModel by viewModels()
    private val binding: SysttsReplaceActivityBinding by lazy {
        SysttsReplaceActivityBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val brv = binding.recyclerView.linear().setup {
            addType<ReplaceRule>(R.layout.systts_replace_rule_item)
            onCreate {
                val binding = getBinding<SysttsReplaceRuleItemBinding>()
                binding.btnEdit.setOnClickListener { edit(getModel()) }
                binding.btnDelete.setOnClickListener { delete(getModel()) }
                binding.checkBox.setOnClickListener {
                    appDb.replaceRuleDao.update(getModel())
                }
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as ReplaceRule).id == (newItem as ReplaceRule).id
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }
        }

        lifecycleScope.launch {
            appDb.replaceRuleDao.flowAll().conflate().collect {
                if (brv.models == null)
                    brv.models = it
                else withDefault { brv.setDifferModels(it) }
            }
        }
    }

    fun delete(data: ReplaceRule) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.is_confirm_delete)
            .setMessage(data.name)
            .setPositiveButton(R.string.delete) { _, _ -> appDb.replaceRuleDao.delete(data) }
            .setFadeAnim()
            .show()
    }

    private fun edit(data: ReplaceRule) {
        val intent = Intent(this, ReplaceRuleEditActivity::class.java)
        intent.putExtra(KeyConst.KEY_DATA, data)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(StartActivityForResult()) { result ->
        result.data?.apply {
            getParcelableExtra<ReplaceRule>(KeyConst.KEY_DATA)?.let {
                if (result.resultCode == KeyConst.RESULT_ADD)
                    appDb.replaceRuleDao.insert(it)
                else
                    appDb.replaceRuleDao.update(it)
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
                MaterialAlertDialogBuilder(this).setTitle(R.string.import_config).setView(et)
                    .setPositiveButton(R.string.import_from_clip) { _, _ ->
                        val err = vm.importConfig(ClipboardUtils.text.toString())
                        err?.let {
                            longToast("导入失败：$it")
                        }
                    }.setNegativeButton(R.string.import_from_url) { _, _ ->
                        val err = vm.importConfigFromUrl(et.text.toString())
                        err?.let {
                            longToast("导入失败：$it")
                        }
                    }.show()
            }

            R.id.menu_exportConfig -> {
                val jsonStr = vm.exportConfig()
                val tv = TextView(this)
                tv.text = jsonStr
                tv.setPadding(50, 50, 50, 0)
                MaterialAlertDialogBuilder(this).setTitle(R.string.export_config).setView(tv)
                    .setPositiveButton(R.string.copy) { _, _ ->
                        ClipboardUtils.copyText(jsonStr)
                        toast(R.string.copied)
                    }.setNegativeButton("上传到URL") { _, _ ->
                        val ret = vm.uploadConfigToUrl(jsonStr)
                        if (ret.isSuccess) {
                            ClipboardUtils.copyText(ret.getOrNull())
                            longToast("已复制URL：\n${ret.getOrNull()}")
                        } else {
                            longToast("上传失败：${ret.exceptionOrNull()?.message}")
                        }
                    }
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }


}