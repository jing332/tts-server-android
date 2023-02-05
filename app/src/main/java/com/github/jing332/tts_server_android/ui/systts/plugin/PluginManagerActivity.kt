package com.github.jing332.tts_server_android.ui.systts.plugin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
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
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.util.clickWithThrottle
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class PluginManagerActivity : BackActivity() {
    val binding by lazy { SysttsPluginManagerActivityBinding.inflate(layoutInflater) }

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.apply {
                getParcelableExtra<Plugin>(KeyConst.KEY_DATA)?.let {
                    appDb.pluginDao.insert(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val brv = binding.rv.linear().setup {
            addType<PluginModel>(R.layout.systts_plguin_list_item)
            onCreate {
                getBinding<SysttsPlguinListItemBinding>().apply {
                    btnDelete.clickWithThrottle {
                        val model = getModel<PluginModel>()
                        AppDialogs.displayDeleteDialog(
                            this@PluginManagerActivity,
                            model.name
                        ) { appDb.pluginDao.delete(model.data) }
                    }
                    btnEdit.clickWithThrottle {
                        startForResult.launch(
                            Intent(
                                this@PluginManagerActivity,
                                PluginEditActivity::class.java
                            ).apply { putExtra(KeyConst.KEY_DATA, getModel<PluginModel>().data) })
                    }
                    cbSwitch.setOnClickListener {
                        appDb.pluginDao.update(getModel<PluginModel>().data.copy(isEnabled = cbSwitch.isChecked))
                    }
                }
            }
        }

        lifecycleScope.launch {
            appDb.pluginDao.flowAll().conflate().collect { list ->
                val models = list.map { PluginModel(it) }
                if (brv.models == null) withMain { brv.models = models }
                else withDefault { brv.setDifferModels(models) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.plugin_manager, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                startForResult.launch(Intent(this, PluginEditActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}