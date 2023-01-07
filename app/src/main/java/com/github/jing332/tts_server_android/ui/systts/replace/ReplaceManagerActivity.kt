package com.github.jing332.tts_server_android.ui.systts.replace

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult.*
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.databinding.SysttsReplaceActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsReplaceRuleItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ReplaceManagerActivity : BackActivity() {
    companion object {
        const val TAG = "ReplaceManagerActivity"
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
            addType<ReplaceRuleModel>(R.layout.systts_replace_rule_item)
            onCreate {
                val binding = getBinding<SysttsReplaceRuleItemBinding>()

                binding.btnEdit.clickWithThrottle { edit(getModel<ReplaceRuleModel>().data) }
                binding.btnDelete.clickWithThrottle { delete(getModel<ReplaceRuleModel>().data) }
                binding.checkBox.setOnClickListener {
                    appDb.replaceRuleDao.update(getModel<ReplaceRuleModel>().data)
                }
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as ReplaceRuleModel).data.id == (newItem as ReplaceRuleModel).data.id
                }

                override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                    val old = oldItem as ReplaceRuleModel
                    val new = newItem as ReplaceRuleModel
                    return old.data == new.data
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {
                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    updateOrder(models)
                }

            })
        }

        lifecycleScope.launch {
            var job: Job? = null
            appDb.replaceRuleDao.flowAll().conflate().collect { list ->
                val models = list.map { ReplaceRuleModel(data = it) }
                if (brv.models == null) withMain { brv.models = models }
                else {
                    job?.cancel()
                    job = null
                    job = launch { delay(100); brv.setDifferModels(models) }
                        .apply { start() }
                }
            }
        }
    }

    fun updateOrder(models: List<Any?>?) {
        models?.forEachIndexed { index, value ->
            val model = value as ReplaceRuleModel
            model.data.order = index
            appDb.replaceRuleDao.update(model.data)
        }
    }

    fun delete(data: ReplaceRule) {
        AppDialogs.displayDeleteDialog(this, data.name) {
            appDb.replaceRuleDao.delete(data)
        }
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
                    appDb.replaceRuleDao.insert(it.apply { order = appDb.replaceRuleDao.count })
                else appDb.replaceRuleDao.update(it)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
        MenuCompat.setGroupDividerEnabled(menu, true)

        menuInflater.inflate(R.menu.menu_replace_manager, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_switch)?.let { menuItem ->
            menuItem.isChecked = SysTtsConfig.isReplaceEnabled
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                val intent = Intent(this, ReplaceRuleEditActivity::class.java)
                startForResult.launch(intent)
            }
            R.id.menu_switch -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isReplaceEnabled = item.isChecked
            }
            R.id.menu_importConfig -> {
                val et = MaterialTextInput(this)
                et.inputLayout.setHint(R.string.url_net)
                MaterialAlertDialogBuilder(this).setTitle(R.string.import_config).setView(et)
                    .setPositiveButton(R.string.import_from_clipboard) { _, _ ->
                        val err = vm.importConfig(ClipboardUtils.text.toString())
                        err?.let {
                            longToast("${getString(R.string.import_failed)}: $it")
                        }
                    }.setNegativeButton(R.string.import_from_url) { _, _ ->
                        val err = vm.importConfigFromUrl(et.inputEdit.text.toString())
                        err?.let {
                            longToast("${getString(R.string.import_failed)}: $it")
                        }
                    }.show()
            }

            R.id.menu_export_config -> {
                AppDialogs.displayExportDialog(this, lifecycleScope, vm.configToJson())
            }
        }

        return super.onOptionsItemSelected(item)
    }


}