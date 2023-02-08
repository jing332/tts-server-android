package com.github.jing332.tts_server_android.ui.base.import1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Net
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.BaseConfigImportActivityBinding
import com.github.jing332.tts_server_android.databinding.BaseConfigImportItemBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseConfigImportActivity : BackActivity() {
    companion object {
        const val SRC_CLIPBOARD = 1
        const val SRC_FILE = 2
        const val SRC_URL = 3
    }

    protected val source: Int
        get() {
            return when (binding.groupSource.checkedButtonId) {
                R.id.btn_src_clipboard -> SRC_CLIPBOARD
                R.id.btn_src_local_file -> SRC_FILE
                R.id.btn_src_url -> SRC_URL
                else -> SRC_CLIPBOARD
            }
        }

    private val binding by lazy { BaseConfigImportActivityBinding.inflate(layoutInflater) }

    protected val waitDialog by lazy { WaitDialog(this) }

    override fun setContentView(view: View?) {
        binding.content.removeAllViews()
        binding.content.addView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(binding.root)

        binding.apply {
            groupSource.check(R.id.btn_src_clipboard)
            groupSource.addOnButtonCheckedListener { _, checkedId, isChecked ->
                when (checkedId) {
                    R.id.btn_src_url -> tilUrl.isGone = !isChecked
                    R.id.btn_src_local_file -> tilFilePath.isGone = !isChecked
                }
            }

            tilFilePath.setEndIconOnClickListener { filePicker() }

            btnImport.clickWithThrottle {
                waitDialog.show()
                lifecycleScope.launch(Dispatchers.Main) {
                    val json = try {
                        withIO {
                            importConfig(
                                source,
                                binding.tilUrl.editText?.text.toString(),
                                binding.tilFilePath.editText?.text.toString()
                            )
                        }
                    } catch (e: Exception) {
                        AppDialogs.displayErrorDialog(
                            this@BaseConfigImportActivity,
                            e.stackTraceToString()
                        )
                        return@launch
                    } finally {
                        waitDialog.dismiss()
                    }

                    kotlin.runCatching { onImport(json) }.onFailure {
                        AppDialogs.displayErrorDialog(
                            this@BaseConfigImportActivity,
                            it.stackTraceToString()
                        )
                    }
                }
            }
        }
    }

    abstract fun onImport(json: String)

    private suspend fun importConfig(
        src: Int,
        url: String? = null,
        fileUri: String? = null
    ): String {
        return when (src) {
            SRC_URL -> withContext(Dispatchers.IO) {
                Net.get(url.toString()).execute()
            }

            SRC_FILE -> withContext(Dispatchers.IO) {
                val input = contentResolver.openInputStream(Uri.parse(fileUri))
                val str = input!!.readBytes().decodeToString()
                input.close()
                return@withContext str
            }

            else -> ClipboardUtils.text.toString()
        }
    }


    private val activityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            waitDialog.dismiss()
            waitDialog.setCancelable(true)
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.let { uri ->
                    binding.tilFilePath.editText!!.setText(uri.toString())

                }
            }
        }

    private fun filePicker() {
        waitDialog.show()
        waitDialog.setCancelable(false)
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            activityForResult.launch(Intent.createChooser(this, "Choose a file"))
        }
    }


    fun displayListSelectDialog(
        list: List<ConfigImportItemModel>,
        onSelectedList: (list: List<Any>) -> Unit
    ) {
        val rv = RecyclerView(this).apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            linear().setup {
                addType<ConfigImportItemModel>(R.layout.base_config_import_item)
                onCreate {
                    getBinding<BaseConfigImportItemBinding>().apply {
                        itemView.setOnClickListener {
                            checkBox.isChecked = !checkBox.isChecked
                        }
                    }
                }
            }.models = list
        }
        rv.setPadding(64.dp)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_import)
            .setView(rv)
            .setPositiveButton(R.string.import_config) { _, _ ->
                onSelectedList.invoke(
                    rv.models!!.filter { it is ConfigImportItemModel && it.isEnabled }
                        .map { (it as ConfigImportItemModel).data }
                )
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

}