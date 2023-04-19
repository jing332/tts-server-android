package com.github.jing332.tts_server_android.ui.systts.plugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.databinding.SysttsPlguinListItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsPluginManagerActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.systts.BrvItemTouchHelper
import com.github.jing332.tts_server_android.ui.systts.replace.GroupModel
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.utils.MyTools
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class PluginManagerActivity : BackActivity() {
    val binding by lazy { SysttsPluginManagerActivityBinding.inflate(layoutInflater) }
    val vm: PluginManagerViewModel by viewModels()

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.apply {
                getParcelableExtra<Plugin>(KeyConst.KEY_DATA)?.let {
                    appDb.pluginDao.insert(it)
                }
            }
        }

    @ExperimentalBadgeUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val brv = binding.rv.linear().setup {
            addType<PluginModel>(R.layout.systts_plguin_list_item)
            onCreate {
                getBinding<SysttsPlguinListItemBinding>().apply {
                    btnOptions.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            val data = getModel<PluginModel>().data
                            val badge = BadgeDrawable.create(this@PluginManagerActivity)
                            badge.isVisible = false
                            badge.badgeGravity = BadgeDrawable.TOP_END
                            badge.isVisible = data.defVars.isNotEmpty() && data.userVars.isEmpty()
                            badge.number = data.defVars.size
                            BadgeUtils.attachBadgeDrawable(badge, btnOptions)
                            btnOptions.tag = badge

                            btnOptions.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })

                    cardView.clickWithThrottle {
                        displayVarsSettings(getModel())
                    }
                    btnOptions.clickWithThrottle {
                        val model = getModel<PluginModel>()

                        PopupMenu(this@PluginManagerActivity, it).apply {
                            menuInflater.inflate(R.menu.systts_plugin_item, menu)
                            MenuCompat.setGroupDividerEnabled(menu, true)
                            setForceShowIcon(true)

                            setOnMenuItemClickListener { menuItem ->
                                when (menuItem.itemId) {
                                    R.id.menu_set_vars -> displayVarsSettings(model)

                                    R.id.menu_remove -> {
                                        AppDialogs.displayDeleteDialog(
                                            this@PluginManagerActivity, model.title
                                        ) { appDb.pluginDao.delete(model.data) }
                                    }
                                }

                                true
                            }
                            show()
                        }
                    }
                    btnEdit.clickWithThrottle {
                        startForResult.launch(
                            Intent(
                                this@PluginManagerActivity,
                                PluginEditorActivity::class.java
                            ).apply { putExtra(KeyConst.KEY_DATA, getModel<PluginModel>().data) })
                    }
                    cbSwitch.setOnClickListener {
                        appDb.pluginDao.update(getModel<PluginModel>().data.copy(isEnabled = cbSwitch.isChecked))
                    }
                }
            }

            onBind {
                getBinding<SysttsPlguinListItemBinding>().apply {
                    val model = getModel<PluginModel>()
                    val badge = btnOptions.tag as? BadgeDrawable
                    badge?.isVisible =
                        model.data.defVars.isNotEmpty() && model.data.userVars.isEmpty()
                    badge?.number = model.data.defVars.size
                }
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {
                override fun onMove(
                    recyclerView: RecyclerView,
                    source: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return if (BrvItemTouchHelper.onMove<GroupModel>(
                            recyclerView, source, target
                        )
                    ) {
                        super.onMove(recyclerView, source, target)
                    } else false
                }


                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    models?.filterIsInstance<PluginModel>()?.let { models ->
                        appDb.pluginDao.update(*models.mapIndexed { index, t ->
                            t.data.apply { order = index }
                        }.toTypedArray())
                    }
                }
            })

            itemDifferCallback = object : ItemDifferCallback {
                override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as PluginModel).data == (newItem as PluginModel).data
                }

                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as PluginModel).data.id == (newItem as PluginModel).data.id
                }

            }

        }

        lifecycleScope.launch {
            appDb.pluginDao.flowAll().conflate().collect { list ->
                val models = list.map { PluginModel(it) }
                withMain {
                    if (brv.models == null) brv.models = models
                    else withDefault { brv.setDifferModels(models) }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.plugin_manager, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
            menu.setGroupDividerEnabled(true)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                startForResult.launch(Intent(this, PluginEditorActivity::class.java))
            }

            R.id.menu_shortcut -> {
                MyTools.addShortcut(
                    this,
                    getString(R.string.plugin_manager),
                    "plugin_manager",
                    R.drawable.ic_plugin,
                    Intent(this, PluginManagerActivity::class.java)
                )
            }

            R.id.menu_export -> {
                val fragment = ExportBottomSheetFragment.newInstance("ttsrv-plugins.json")
                fragment.show(supportFragmentManager, "ExportBottomSheetFragment")
            }

            R.id.menu_import -> {
                val fragment = ImportBottomSheetFragment()
                fragment.show(supportFragmentManager, ImportBottomSheetFragment.TAG)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayVarsSettings(model: PluginModel) {
        val fragment = PluginVarsSettingsBottomSheetFragment.newInstance(model.title, model.data)
        fragment.show(supportFragmentManager, "PluginVarsSettingsBottomSheetFragment")

    }
}