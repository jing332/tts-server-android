package com.github.jing332.tts_server_android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager


class FilePickerActivity : AppCompatActivity() {
    companion object {
        const val KEY_TYPE = "type"
        const val KEY_FILE_DATA = "file_data"
        const val KEY_FILE_NAME = "file_name"

        const val TYPE_SAVE_FILE = 0
        const val TYPE_SELECT_FILE = 1
        const val TYPE_SELECT_DIR = 2

        private const val REQUEST_CODE_SAVE_FILE = 123321
    }

    private var useSystem: Boolean = false
    private val docTreeSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            setResult(RESULT_OK, Intent().apply { data = it })
            finish()
        }

    private val docSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            setResult(RESULT_OK, Intent().apply { data = it })
            finish()
        }

    private var fileName: String = "ttsrv-fileName.txt"
    private var savedFileData: ByteArray? = null
    private val docCreate =
        FileUtils.registerResultCreateDocument(this, "text/*") { savedFileData }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    setResult(RESULT_OK, Intent().setData(list.component1().toUri()))
                    finish()
                }
            }

            REQUEST_CODE_SAVE_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileDir = FilePickerManager.obtainData().getOrNull(0)
                    if (fileDir == null) {
                        toast("目录为空！")
                    } else {
                        if (savedFileData == null) toast("文件数据为空")
                        else
                            FileUtils.saveFile(fileDir + "/${fileName}", savedFileData!!)
                    }
                }
                finish()
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lp = window.attributes
        lp.alpha = 0.0f
        window.attributes = lp

        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_baseline_file_open_24)
            .setTitle("文件选择器")
            .setItems(arrayOf("系统文件选择器", "内置文件选择器")) { _, which ->
                useSystem = which == 0
                when (intent.getIntExtra(KEY_TYPE, 1)) {
                    TYPE_SAVE_FILE -> {
                        savedFileData = intent.getByteArrayExtra(KEY_FILE_DATA)
                        fileName = intent.getStringExtra(KEY_FILE_NAME) ?: ""
                        saveFile(fileName)
                    }

                    TYPE_SELECT_FILE -> selectFile()
                    TYPE_SELECT_DIR -> selectDir()
                }
            }
            .setPositiveButton(R.string.cancel) { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }

    private fun saveFile(fileName: String) {
        if (useSystem)
            kotlin.runCatching {
                docCreate.launch(fileName)
            }.onFailure {
                toast(R.string.sys_doc_picker_error)
                useSystem = true
                return saveFile(fileName)
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
            .skipDirWhenSelect(false)
            .forResult(requestCode)
    }


    @Suppress("ArrayInDataClass")
    data class RequestData(
        val useSystem: Boolean = false,
        val type: Int,
        val fileName: String? = null,
        val fileMime: String? = null,
        val fileData: ByteArray? = null
    )
}