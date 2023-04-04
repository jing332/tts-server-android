package com.github.jing332.tts_server_android.ui.systts.speech_rule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.lifecycleScope
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleManagerActivityBinding
import com.github.jing332.tts_server_android.ui.base.AppBackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.clickWithThrottle
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class SpeechRuleManagerActivity : AppBackActivity<SysttsSpeechRuleManagerActivityBinding>() {
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
                            putExtra(KeyConst.KEY_DATA, model.data)
                        })
                    }
                    cbSwitch.setOnClickListener {
                        appDb.speechRule.update(getModel<SpeechRuleModel>().data.copy(isEnabled = cbSwitch.isChecked))
                    }
                }
            }

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
            R.id.menu_add -> {
                startForResult.launch(Intent(this, SpeechRuleEditorActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }
}