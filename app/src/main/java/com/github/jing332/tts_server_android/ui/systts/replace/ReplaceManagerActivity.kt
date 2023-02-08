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
import com.github.jing332.tts_server_android.databinding.SysttsReplaceActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsReplaceRuleItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.base.group.GroupListHelper
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.checkbox.MaterialCheckBox
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

    private var savedData: ByteArray? = null
    private val getFileUriToSave =
        FileUtils.registerResultCreateDocument(this, "application/json") { savedData }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        brv = binding.recyclerView.linear().setup {
            addType<ItemModel>(R.layout.systts_replace_rule_item)
            addType<GroupModel>(R.layout.base_list_group_item)

            onCreate {
                val groupHelper = GroupListHelper<GroupModel>(this@ReplaceManagerActivity)
                groupHelper.callback = object : GroupListHelper.Callback<GroupModel> {
                    override fun onGroupClick(v: View, model: GroupModel) {
                        val data = getModel<GroupModel>().data
                        val isExpanded = !data.isExpanded
                        appDb.replaceRuleDao.insertGroup(data.copy(isExpanded = isExpanded))
                    }

                    override fun onCheckedChange(v: MaterialCheckBox, model: GroupModel) {
                        model.itemSublist?.forEach {
                            if (it is ItemModel) {
                                appDb.replaceRuleDao.update(it.data.copy(isEnabled = v.isChecked))
                            }
                        }
                    }


                    override fun onExport(v: View, model: GroupModel) {
                        AppDialogs.displayExportDialog(
                            this@ReplaceManagerActivity, lifecycleScope, vm.exportGroup(model)
                        ) {
                            savedData = it.toByteArray()
                            getFileUriToSave.launch("ttsrv-replaces.json")
                        }
                    }

                    override fun onDelete(v: View, model: GroupModel) {
                        AppDialogs.displayDeleteDialog(this@ReplaceManagerActivity, model.name) {
                            appDb.replaceRuleDao.deleteGroup(model.data)
                            appDb.replaceRuleDao.deleteAllByGroup(model.data.id)
                        }
                    }

                    override fun onRename(v: View, model: GroupModel) {
                        AppDialogs.displayInputDialog(
                            this@ReplaceManagerActivity,
                            getString(R.string.edit_group_name),
                            getString(R.string.name),
                            model.name
                        ) {
                            appDb.replaceRuleDao.insertGroup(model.data.copy(name = it))
                        }
                    }
                }
                groupHelper.initGroup(this@setup, this)

                getBindingOrNull<SysttsReplaceRuleItemBinding>()?.apply {
                    btnEdit.clickWithThrottle { edit(getModel<ItemModel>().data) }
                    btnMore.clickWithThrottle {
                        displayMoreMenu(it, getModel<ItemModel>().data)
                    }
                    checkBox.setOnClickListener {
                        appDb.replaceRuleDao.update(getModel<ItemModel>().data)
                    }
                }
            }


            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return if (oldItem is ItemModel && newItem is ItemModel) oldItem.data.id == newItem.data.id
                    else if (oldItem is GroupModel && newItem is GroupModel) oldItem.data.id == newItem.data.id
                    else false
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?, actionState: Int
                ) {
                    if (viewHolder != null && getModel<Any>(viewHolder.layoutPosition) is GroupModel && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        (viewHolder as BindingAdapter.BindingViewHolder).collapse()
                    }

                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    if (source.getModel<Any>() is GroupModel) {
                        models?.filterIsInstance<GroupModel>()?.let { models ->
                            models.forEachIndexed { index, value ->
                                appDb.replaceRuleDao.updateGroup(value.data.apply { order = index })
                            }
                        }
                    } else {
                        val groupModel = models?.get(source.findParentPosition()) as GroupModel
                        val listInGroup =
                            models!!.filter { it is ItemModel && groupModel.data.id == it.data.groupId }
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
                SearchTargetFilter.list.forEachIndexed { index, v ->
                    val item = menu.add(Menu.NONE, index, index, v.strId)
                    item.isCheckable = true
                    if (currentSearchTarget == v) item.isChecked = true
                }

                setOnMenuItemClickListener { menuItem ->
                    SearchTargetFilter.list.forEachIndexed { index, _ ->
                        menu.findItem(index).isChecked = false
                    }
                    menuItem.isChecked = true
                    currentSearchTarget = SearchTargetFilter.list[menuItem.itemId]
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

        if (appDb.replaceRuleDao.getGroup() == null) appDb.replaceRuleDao.insertGroup(
            ReplaceRuleGroup(
                id = AbstractListGroup.DEFAULT_GROUP_ID, name = getString(
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


    private var mLastModels: List<GroupWithReplaceRule>? = null

    private suspend fun updateListModels(list: List<GroupWithReplaceRule>? = mLastModels) {
        mLastModels = list
        mLastModels?.let { lastModels ->
            val filterText = binding.etSearch.text
            val models = lastModels.map { data ->
                val filteredList = data.list.filter {
                    val pro = when (currentSearchTarget) {
                        SearchTargetFilter.Name -> it.name
                        SearchTargetFilter.Pattern -> it.pattern
                        SearchTargetFilter.Replacement -> it.replacement
                        SearchTargetFilter.Group -> appDb.replaceRuleDao.getGroup(it.groupId)?.name.toString()
                    }
                    pro.contains(filterText)
                }.sortedBy { it.order }
                val checkedState = when (filteredList.filter { it.isEnabled }.size) {
                    0 -> MaterialCheckBox.STATE_UNCHECKED           // 全未选
                    filteredList.size -> MaterialCheckBox.STATE_CHECKED   // 全选
                    else -> MaterialCheckBox.STATE_INDETERMINATE    // 部分选
                }

                GroupModel(
                    data = data.group,
                    itemSublist = filteredList.map { ItemModel(it) },
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
            val model = value as ItemModel
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
                appDb.replaceRuleDao.insert(it)
                if (it.isEnabled) SystemTtsService.notifyUpdateConfig()
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
                    this, getString(R.string.edit_group_name), getString(R.string.name)
                ) { text ->
                    appDb.replaceRuleDao.insertGroup(ReplaceRuleGroup(name = text.ifEmpty {
                        getString(R.string.unnamed)
                    }))
                }
            }

            R.id.menu_switch -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isReplaceEnabled = item.isChecked
            }

            R.id.menu_importConfig -> {
                startActivity(Intent(this, ConfigImportActivity::class.java))
            }

            R.id.menu_export_config -> {
                AppDialogs.displayExportDialog(this, lifecycleScope, vm.configToJson()) {
                    savedData = it.toByteArray()
                    getFileUriToSave.launch("ttsrv-replaces.json")
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    sealed class SearchTargetFilter(val strId: Int, val index: Int) {
        companion object {
            val list by lazy {
                listOf(
                    Name,
                    Pattern,
                    Replacement,
                    Group,
                )
            }
        }

        object Name : SearchTargetFilter(R.string.display_name, 0)
        object Pattern : SearchTargetFilter(R.string.systts_replace_rule, 1)
        object Replacement : SearchTargetFilter(R.string.systts_replace_as, 2)
        object Group : SearchTargetFilter(R.string.group, 3)
    }
}