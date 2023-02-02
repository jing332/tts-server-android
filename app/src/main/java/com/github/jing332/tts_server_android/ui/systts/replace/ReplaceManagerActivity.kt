package com.github.jing332.tts_server_android.ui.systts.replace

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.databinding.SysttsListCustomGroupItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsReplaceActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsReplaceRuleItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ReplaceManagerActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ReplaceManagerActivity"
    }

    private val vm: ReplaceManagerViewModel by viewModels()
    private val binding: SysttsReplaceActivityBinding by lazy {
        SysttsReplaceActivityBinding.inflate(layoutInflater)
    }

    private lateinit var brv: BindingAdapter

    private var currentSearchTarget: SearchTargetFilter = SearchTargetFilter.Name

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        brv = binding.recyclerView.linear().setup {
            addType<ReplaceRuleGroupModel>(R.layout.systts_list_custom_group_item)
            addType<ReplaceRuleModel>(R.layout.systts_replace_rule_item)
            onCreate {
                getBindingOrNull<SysttsListCustomGroupItemBinding>()?.apply {
                    itemView.clickWithThrottle {
                        val data = getModel<ReplaceRuleGroupModel>().data
                        appDb.replaceRuleDao.insertGroup(data.copy(isExpanded = !data.isExpanded))
                    }
                    btnMore.clickWithThrottle {
                        displayGroupMenu(
                            it, getModel()
                        )
                    }
                    checkBox.setOnClickListener {
                        val model = getModel<ReplaceRuleGroupModel>()
                        model.itemSublist?.forEach {
                            if (it is ReplaceRuleModel) {
                                appDb.replaceRuleDao.update(it.data.copy(isEnabled = checkBox.isChecked))
                            }
                        }
                    }
                }

                getBindingOrNull<SysttsReplaceRuleItemBinding>()?.apply {
                    btnEdit.clickWithThrottle { edit(getModel<ReplaceRuleModel>().data) }
                    btnMore.clickWithThrottle {
                        displayMoreMenu(it, getModel<ReplaceRuleModel>().data)
                    }
                    checkBox.setOnClickListener {
                        appDb.replaceRuleDao.update(getModel<ReplaceRuleModel>().data)
                    }
                }

            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return if (oldItem is ReplaceRuleModel && newItem is ReplaceRuleModel)
                        oldItem.data.id == newItem.data.id
                    else if (oldItem is ReplaceRuleGroupModel && newItem is ReplaceRuleGroupModel)
                        oldItem.data.id == newItem.data.id
                    else
                        false
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {

                @Suppress("KotlinConstantConditions")
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    if (viewHolder != null
                        && getModel<Any>(viewHolder.layoutPosition) is ReplaceRuleGroupModel
                        && actionState == ItemTouchHelper.ACTION_STATE_DRAG
                    ) {
                        (viewHolder as BindingAdapter.BindingViewHolder).collapse()
                    }

                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    if (source.getModel<Any>() is ReplaceRuleGroupModel) {
                        models?.filterIsInstance<ReplaceRuleGroupModel>()?.let { models ->
                            models.forEachIndexed { index, value ->
                                appDb.replaceRuleDao.updateGroup(value.data.apply { order = index })
                            }
                        }
                    } else {
                        val groupModel =
                            models?.get(source.findParentPosition()) as ReplaceRuleGroupModel
                        val listInGroup =
                            models!!.filter { it is ReplaceRuleModel && groupModel.data.id == it.data.groupId }
                        updateOrder(listInGroup)
                    }
                }
            })
        }
        binding.etSearch.addTextChangedListener {
            lifecycleScope.launch { updateListModels() }
        }
        binding.btnTarget.clickWithThrottle { view ->
            PopupMenu(this, view).apply {
                val list = listOf(
                    SearchTargetFilter.Name,
                    SearchTargetFilter.Pattern,
                    SearchTargetFilter.Replacement
                )
                list.forEachIndexed { index, v ->
                    val item = menu.add(Menu.NONE, index, index, v.strId)
                    item.isCheckable = true
                    if (currentSearchTarget == v)
                        item.isChecked = true
                }

                setOnMenuItemClickListener { menuItem ->
                    list.forEachIndexed { index, _ ->
                        menu.findItem(index).isChecked = false
                    }
                    menuItem.isChecked = true
                    currentSearchTarget = list[menuItem.itemId]
                    lifecycleScope.launch { updateListModels() }
                    true
                }

                show()
            }
        }

        lifecycleScope.launch {
            appDb.replaceRuleDao.flowAllGroupWithReplaceRules().conflate().collect { list ->
                updateListModels(list)
            }
        }

        if (!SysTtsConfig.isReplaceEnabled) toast(R.string.systts_replace_please_on_switch)

        if (appDb.replaceRuleDao.getGroup() == null)
            appDb.replaceRuleDao.insertGroup(
                ReplaceRuleGroup(
                    id = AbstractListGroup.DEFAULT_GROUP_ID,
                    name = getString(
                        R.string.default_group
                    )
                )
            )
    }

    private fun displayMoreMenu(v: View, data: ReplaceRule) {
        PopupMenu(this, v).apply {
            setForceShowIcon(true)
            MenuCompat.setGroupDividerEnabled(menu, true)
            menuInflater.inflate(R.menu.systts_replace_rule_more, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_delete -> {
                        AppDialogs.displayDeleteDialog(this@ReplaceManagerActivity, data.name) {
                            appDb.replaceRuleDao.delete(data)
                        }
                    }
                    R.id.menu_move_to_top -> {
                        appDb.replaceRuleDao.updateDataAndRefreshOrder(data.copy(order = 0))
                    }
                    R.id.menu_move_to_bottom -> {
                        appDb.replaceRuleDao.updateDataAndRefreshOrder(data.copy(order = appDb.replaceRuleDao.count))
                    }
                }
                true
            }
        }.show()
    }

    private fun displayGroupMenu(v: View, model: ReplaceRuleGroupModel) {
        PopupMenu(this, v).apply {
            this.setForceShowIcon(true)
            MenuCompat.setGroupDividerEnabled(menu, true)
            menuInflater.inflate(R.menu.systts_list_group_more, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_export_config -> {
                        AppDialogs.displayExportDialog(
                            this@ReplaceManagerActivity,
                            lifecycleScope, vm.exportGroup(model)
                        )
                    }
                    R.id.menu_rename_group -> {
                        AppDialogs.displayInputDialog(
                            this@ReplaceManagerActivity,
                            getString(R.string.edit_group_name),
                            getString(R.string.name),
                            model.name
                        ) {
                            appDb.replaceRuleDao.insertGroup(model.data.copy(name = it))
                        }
                    }
                    R.id.menu_delete_group -> {
                        AppDialogs.displayDeleteDialog(this@ReplaceManagerActivity, model.name) {
                            appDb.replaceRuleDao.deleteGroup(model.data)
                            appDb.replaceRuleDao.deleteAllByGroup(model.data.id)
                        }
                    }
                }
                true
            }
            show()
        }

    }

    private var mLastModels: List<GroupWithReplaceRule>? = null

    private suspend fun updateListModels(list: List<GroupWithReplaceRule>? = mLastModels) {
        mLastModels = list
        mLastModels?.let { lastModels ->
        val filterText = binding.etSearch.text
        val models =
            lastModels.map { data ->
                val checkedState =
                    when (data.list.filter { it.isEnabled }.size) {
                        0 -> MaterialCheckBox.STATE_UNCHECKED           // 全未选
                        data.list.size -> MaterialCheckBox.STATE_CHECKED   // 全选
                        else -> MaterialCheckBox.STATE_INDETERMINATE    // 部分选
                    }

                ReplaceRuleGroupModel(
                    data = data.group,
                    itemSublist = data.list.filter {
                        val pro = when (currentSearchTarget) {
                            SearchTargetFilter.Name -> it.name
                            SearchTargetFilter.Pattern -> it.pattern
                            SearchTargetFilter.Replacement -> it.replacement
                        }
                        pro.contains(filterText)
                    }.sortedBy { it.order }.map { ReplaceRuleModel(it) },
                    itemExpand = filterText.isNotBlank() || data.group.isExpanded,
                    checkedState = checkedState
                )
            }.filter { filterText.isEmpty() || it.itemSublist?.isNotEmpty() == true }

        if (brv.models == null) withMain { brv.models = models }
        else brv.setDifferModels(models)
        }
    }

    fun updateOrder(models: List<Any?>?) {
        models?.forEachIndexed { index, value ->
            val model = value as ReplaceRuleModel
            appDb.replaceRuleDao.update(model.data.copy(order = index))
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
        menuInflater.inflate(R.menu.systts_replace_manager, menu)

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
            R.id.menu_add_group -> {
                AppDialogs.displayInputDialog(
                    this,
                    getString(R.string.edit_group_name),
                    getString(R.string.name)
                ) { text ->
                    appDb.replaceRuleDao.insertGroup(ReplaceRuleGroup(name = text))
                }
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

    sealed class SearchTargetFilter(val strId: Int, val index: Int) {
        object Name : SearchTargetFilter(R.string.display_name, 0)
        object Pattern : SearchTargetFilter(R.string.systts_replace_rule, 1)
        object Replacement : SearchTargetFilter(R.string.systts_replace_as, 2)

    }
}