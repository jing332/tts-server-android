package com.github.jing332.tts_server_android.ui.systts.speech_rule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.runMain
import com.drake.net.utils.withDefault
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleHanlpSettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsSpeechRuleManagerActivityBinding
import com.github.jing332.tts_server_android.model.hanlp.HanlpManager
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.systts.BrvItemTouchHelper
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.replace.GroupModel
import com.github.jing332.tts_server_android.ui.view.ActivityTransitionHelper.initExitSharedTransition
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.ProgressLoadingDialog
import com.github.jing332.tts_server_android.utils.ZipUtils
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import java.util.zip.ZipInputStream

class SpeechRuleManagerActivity : BackActivity() {
    private val binding by lazy { SysttsSpeechRuleManagerActivityBinding.inflate(layoutInflater) }

    private lateinit var brv: BindingAdapter

    private val editorForResult = registerForActivityResult(
        AppActivityResultContracts.parcelableDataActivity<SpeechRule>(SpeechRuleEditorActivity::class.java)
    ) { rule ->
        rule?.let {
            appDb.speechRule.insert(it)
        }
    }

    private fun startEditor(rule: SpeechRule? = null, itemView: View? = null) {
        if (itemView == null) {
            editorForResult.launch(rule)
        } else {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, itemView,
                getString(R.string.key_activity_shared_container_trans)
            )
            editorForResult.launch(rule, options)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initExitSharedTransition()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent?.getStringExtra("js")?.let { js ->
            startEditor(SpeechRule(code = js, name = "New Speech Rule"))
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

                    btnEdit.clickWithThrottle {
                        val model = getModel<SpeechRuleModel>()
                        startEditor(model.data, root)
                    }
                    btnOptions.clickWithThrottle {
                        val model = getModel<SpeechRuleModel>()

                        PopupMenu(this@SpeechRuleManagerActivity, it).apply {
                            menuInflater.inflate(R.menu.systts_speech_rule_item, menu)
                            MenuCompat.setGroupDividerEnabled(menu, true)
                            setForceShowIcon(true)

                            setOnMenuItemClickListener { menuItem ->
                                when (menuItem.itemId) {
                                    R.id.menu_export -> {
                                        exportConfig(
                                            listOf(model.data),
                                            fileName = "ttsrv-speechRules-${model.data.name}.json"
                                        )
                                    }

                                    R.id.menu_remove -> {
                                        AppDialogs.displayDeleteDialog(
                                            this@SpeechRuleManagerActivity, model.title
                                        ) { appDb.speechRule.delete(model.data) }
                                    }
                                }

                                true
                            }
                            show()
                        }
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

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
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
        menuInflater.inflate(R.menu.systts_speech_rule_manager, menu)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)

        return super.onCreateOptionsMenu(menu)
    }

    private fun exportConfig(list: List<SpeechRule>, fileName: String = "ttsrv-speechRules.json") {
        val exportFragment = ConfigExportBottomSheetFragment({
            AppConst.jsonBuilder.encodeToString(list)
        }, { fileName })
        exportFragment.show(supportFragmentManager, ConfigExportBottomSheetFragment.TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_hanlp_settings -> hanlpSettingsDialog()
            R.id.menu_export -> {
                exportConfig(appDb.speechRule.all)
            }

            R.id.menu_import -> {
                val importFragment = ImportConfigBottomSheetFragment()
                importFragment.show(supportFragmentManager, ImportConfigBottomSheetFragment.TAG)
            }

            R.id.menu_add -> startEditor()

        }

        return super.onOptionsItemSelected(item)
    }

    private val fileSelection =
        registerForActivityResult(AppActivityResultContracts.filePickerActivity()) { ret ->
            ret.second?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                var unzipJob: Job? = null
                unzipJob = lifecycleScope.launch {
                    val docFile = DocumentFile.fromSingleUri(this@SpeechRuleManagerActivity, uri)
                    if (docFile == null) {
                        longToast("文件信息获取失败")
                        return@launch
                    }

                    val zipSize = docFile.length()
                    contentResolver.openInputStream(uri).use { ins ->
                        kotlin.runCatching {
                            ZipInputStream(ins).use { zipIns ->
                                val loadingDialogBuilder =
                                    ProgressLoadingDialog(this@SpeechRuleManagerActivity)
                                        .apply {
                                            setPositiveButton(
                                                R.string.cancel
                                            ) { _, _ ->
                                                unzipJob?.cancel()
                                            }
                                        }
                                val dialog = withMain { loadingDialogBuilder.show() }
                                ZipUtils.unzipFile(
                                    zipIns,
                                    getExternalFilesDir("hanlp")!!,
                                    onProgress = { readCompressedSize, entry ->
                                        val progress =
                                            (readCompressedSize.toFloat() / zipSize.toFloat()) * 10
                                        println("${readCompressedSize}, $zipSize")
                                        runMain {
                                            loadingDialogBuilder.setProgress(progress.toInt())
                                            loadingDialogBuilder.setText("正在导入 ${entry?.name}")
                                        }
                                    }
                                )
                                withMain {
                                    dialog.dismiss()
                                    longToast("完毕")
                                }
                            }
                        }.onFailure {
                            longToast("解压失败：${it.message}")
                        }
                    }
                }.job

                unzipJob.start()
            }
        }

    private fun hanlpSettingsDialog() {
        val viewBinding = SysttsSpeechRuleHanlpSettingsBinding.inflate(layoutInflater, null, false)
        viewBinding.apply {
            btnImport.clickWithThrottle {
                fileSelection.launch(
                    FilePickerActivity.RequestSelectFile(
                        listOf("application/zip", "application/x-zip-compressed")
                    )
                )
            }

            btnTest.clickWithThrottle {
                if (HanlpManager.test())
                    longToast("测试成功")
                else
                    longToast("测试失败，请检查hanlp目录结构是否正确")
            }
        }
        MaterialAlertDialogBuilder(this).setTitle(R.string.hanlp_settings)
            .setView(viewBinding.root)
            .setPositiveButton(R.string.close, null)
            .show()
    }
}