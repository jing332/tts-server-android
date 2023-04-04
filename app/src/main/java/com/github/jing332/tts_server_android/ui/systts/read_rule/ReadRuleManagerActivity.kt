package com.github.jing332.tts_server_android.ui.systts.read_rule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.lifecycleScope
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.databinding.SysttsReadRuleItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsReadRuleManagerActivityBinding
import com.github.jing332.tts_server_android.ui.base.AppBackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.clickWithThrottle
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class ReadRuleManagerActivity : AppBackActivity<SysttsReadRuleManagerActivityBinding>() {
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
            addType<ReadRuleModel>(R.layout.systts_read_rule_item)

            onCreate {
                itemView.clickWithThrottle {
                    val model = getModel<ReadRuleModel>()
                    appDb.speechRule.update(*appDb.speechRule.all.map {
                        it.isEnabled = model.data.id == it.id
                        it
                    }.toTypedArray())
                }
                getBinding<SysttsReadRuleItemBinding>().apply {
                    btnDelete.clickWithThrottle {
                        val model = getModel<ReadRuleModel>()
                        AppDialogs.displayDeleteDialog(
                            this@ReadRuleManagerActivity, model.title
                        ) { appDb.speechRule.delete(model.data) }
                    }
                    btnEdit.clickWithThrottle {
                        val model = getModel<ReadRuleModel>()
                        startForResult.launch(Intent(
                            this@ReadRuleManagerActivity,
                            ReadRuleEditorActivity::class.java
                        ).apply {
                            putExtra(KeyConst.KEY_DATA, model.data)
                        })
                    }
                    cbSwitch.setOnClickListener {
                        appDb.speechRule.update(getModel<ReadRuleModel>().data.copy(isEnabled = cbSwitch.isChecked))
                    }
                }
            }
        }

        lifecycleScope.launch {
            appDb.speechRule.flowAll().conflate().collect { list ->
                brv.models = list.map { ReadRuleModel(it) }
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
                startForResult.launch(Intent(this, ReadRuleEditorActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }
}