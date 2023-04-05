package com.github.jing332.tts_server_android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.FilePickerMode
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager


@Suppress("DEPRECATION")
class FilePickerActivity : AppCompatActivity() {
    companion object {
        const val ACTION_SAVE_FILE = 0
        const val ACTION_SELECT_FILE = 1
        const val ACTION_SELECT_DIR = 2

        private const val REQUEST_CODE_SAVE_FILE = 123321
    }

    private val docTreeSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            setResult(RESULT_OK, Intent().apply {
                data = it
                putExtra(KeyConst.KEY_DATA, requestData)
            })
            finish()
        }

    private val docSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            setResult(RESULT_OK, Intent().apply {
                data = it
                putExtra(KeyConst.KEY_DATA, requestData)
            })
            finish()
        }

    private lateinit var requestData: RequestData
    private val docCreate =
        FileUtils.registerResultCreateDocument(this, "text/*") { requestData.fileData }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(KeyConst.KEY_DATA, requestCode)
                        setData(list.component1().toUri())
                    })
                }
                finish()
            }

            REQUEST_CODE_SAVE_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileDir = FilePickerManager.obtainData().getOrNull(0)
                    if (fileDir == null) {
                        toast(R.string.path_is_empty)
                    } else {
                        if (requestData.fileData == null) toast("文件数据为空")
                        else
                            FileUtils.saveFile(
                                fileDir + "/${requestData.fileName}",
                                requestData.fileData!!
                            )
                    }
                }
                finish()
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private var useSystem = false

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lp = window.attributes
        lp.alpha = 0.0f
        window.attributes = lp

        requestData = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: RequestData()

        when (AppConfig.filePickerMode) {
            FilePickerMode.PROMPT -> {
                MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_baseline_file_open_24)
                    .setTitle(R.string.file_picker)
                    .setItems(
                        arrayOf(
                            getString(R.string.file_picker_mode_system),
                            getString(R.string.file_picker_mode_builtin)
                        )
                    ) { _, which ->
                        useSystem = which == 0
                        doAction()
                    }
                    .setPositiveButton(R.string.cancel) { _, _ ->
                        finish()
                    }
                    .setOnCancelListener {
                        finish()
                    }
                    .show()
            }

            FilePickerMode.SYSTEM -> {
                useSystem = true
                doAction()
            }

            FilePickerMode.Builtin -> {
                useSystem = false
                doAction()
            }

        }
    }

    private fun doAction() {
        when (requestData.action) {
            ACTION_SAVE_FILE -> {
                saveFile()
            }

            ACTION_SELECT_FILE -> selectFile()
            ACTION_SELECT_DIR -> selectDir()
        }
    }

    private fun saveFile() {
        if (useSystem)
            kotlin.runCatching {
                docCreate.launch(requestData.fileName)
            }.onFailure {
                toast(R.string.sys_doc_picker_error)
                useSystem = true
                return saveFile()
            }
        else {
            pickerDir(REQUEST_CODE_SAVE_FILE)
        }
    }

    private fun selectFile() {
        if (useSystem) {
            kotlin.runCatching {
                docSelector.launch(arrayOf("*/*"))
            }.onFailure {
                toast(R.string.sys_doc_picker_error)
                useSystem = true
                return selectFile()
            }
        } else {
            FilePickerManager
                .from(this)
                .maxSelectable(1)
                .showCheckBox(false)
                .enableSingleChoice()
                .forResult(FilePickerManager.REQUEST_CODE)
        }
    }

    private fun selectDir() {
        if (useSystem) {
            kotlin.runCatching {
                docTreeSelector.launch(Uri.EMPTY)
            }.onFailure {
                toast(R.string.sys_doc_picker_error)
                useSystem = true
                return selectDir()
            }
        } else {
            pickerDir()
        }
    }

    private fun pickerDir(requestCode: Int = FilePickerManager.REQUEST_CODE) {
        FilePickerManager
            .from(this)
            .maxSelectable(1)
            .filter(object : AbstractFileFilter() {
                override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                    return ArrayList(listData.filter { item ->
                        item.isDir
                    })
                }
            })
            .enableSingleChoice()
            .skipDirWhenSelect(false)
            .forResult(requestCode)
    }


    @Parcelize
    @Suppress("ArrayInDataClass")
    data class RequestData(
        val action: Int = ACTION_SELECT_FILE,

        val fileName: String? = null,
        val fileMime: String? = null,
        val fileData: ByteArray? = null
    ) : Parcelable
}