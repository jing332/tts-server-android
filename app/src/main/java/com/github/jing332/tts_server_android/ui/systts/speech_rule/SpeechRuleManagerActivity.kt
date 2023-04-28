package com.github.jing332.tts_server_android.ui.systts.speech_rule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleManagerActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.systts.BrvItemTouchHelper
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginEditorActivity
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginModel
import com.github.jing332.tts_server_android.ui.systts.replace.GroupModel
import com.github.jing332.tts_server_android.ui.systts.replace.ItemModel
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class SpeechRuleManagerActivity : BackActivity() {
    private val binding by lazy { SysttsSpeechRuleManagerActivityBinding.inflate(layoutInflater) }

    private lateinit var brv: BindingAdapter

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result?.data?.let { intent ->
                intent.getParcelableExtra<SpeechRule>(KeyConst.KEY_DATA)?.let {
                    appDb.speechRule.insert(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent?.getStringExtra("js")?.let { js ->
            startForResult.launch(
                Intent(
                    this,
                    SpeechRuleEditorActivity::class.java
                ).apply {
                    putExtra(
                        KeyConst.KEY_DATA,
                        SpeechRule(code = js, name = "New Speech Rule")
                    )
                })
        }

        brv = binding.rv.linear().setup {
            addType<SpeechRuleModel>(R.layout.systts_speech_rule_item)

            onCreate {
                itemView.clickWithThrottle {
                    val model = getModel<SpeechRuleModel>()
                    appDb.speechRule.update(*appDb.speechRule.all.map {
                        it.isEnabled = model.data.id == it.id
                        it
                    }.toTypedArray())
                }
                getBinding<SysttsSpeechRuleItemBinding>().apply {
                    btnDelete.clickWithThrottle {
                        val model = getModel<SpeechRuleModel>()
                        AppDialogs.displayDeleteDialog(
                            this@SpeechRuleManagerActivity, model.title
                        ) { appDb.speechRule.delete(model.data) }
                    }
                    btnEdit.clickWithThrottle {
                        val model = getModel<SpeechRuleModel>()
                        startForResult.launch(Intent(
                            this@SpeechRuleManagerActivity,
                            SpeechRuleEditorActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyConst.KEY_DATA, model.data)
                        })
                    }
                    cbSwitch.setOnClickListener {
                        appDb.speechRule.update(getModel<SpeechRuleModel>().data.copy(isEnabled = cbSwitch.isChecked))
                    }
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
                    models?.filterIsInstance<SpeechRuleModel>()?.let { models ->
                        appDb.speechRule.update(*models.mapIndexed { index, t ->
                            t.data.apply { order = index }
                        }.toTypedArray())
                    }
                }
            })

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SpeechRuleModel).data.id == (newItem as SpeechRuleModel).data.id
                }

                override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SpeechRuleModel).data == (newItem as SpeechRuleModel).data
                }
            }
        }

        lifecycleScope.launch {
            appDb.speechRule.flowAll().conflate().collect { list ->
                val models = list.map { SpeechRuleModel(it) }
                if (brv.models == null)
                    brv.models = models
                else {
                    withDefault { brv.setDifferModels(models) }
                }
            }

        }

    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_read_rule_manager, menu)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_export -> {
                val exportFragment = ConfigExportBottomSheetFragment({
                    AppConst.jsonBuilder.encodeToString(appDb.speechRule.allEnabled)
                }, { "ttsrv-speechRules" })
                exportFragment.show(supportFragmentManager, ConfigExportBottomSheetFragment.TAG)
            }

            R.id.menu_import -> {
                val importFragment = ImportConfigBottomSheetFragment()
                importFragment.show(supportFragmentManager, ImportConfigBottomSheetFragment.TAG)
            }

            R.id.menu_add -> {
                startForResult.launch(Intent(this, SpeechRuleEditorActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

        return super.onOptionsItemSelected(item)
    }
}