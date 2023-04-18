package com.github.jing332.tts_server_android.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.FilePickerMode
import com.github.jing332.tts_server_android.help.ByteArrayBinder
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.utils.FileUtils
import com.github.jing332.tts_server_android.utils.FileUtils.mimeType
import com.github.jing332.tts_server_android.utils.getBinder
import com.github.jing332.tts_server_android.utils.grantReadWritePermission
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager
import java.io.File


@Suppress("DEPRECATION")
class FilePickerActivity : AppCompatActivity() {
    companion object {
        const val KEY_REQUEST_DATA = "KEY_REQUEST_DATA"
        private const val REQUEST_CODE_SAVE_FILE = 123321
    }

    private lateinit var requestData: IRequestData

    private val reqSaveFile: RequestSaveFile
        get() = requestData as RequestSaveFile

    private val reqSelectDir: RequestSelectDir
        get() = requestData as RequestSelectDir

    private val reqSelectFile: RequestSelectFile
        get() = requestData as RequestSelectFile

    private lateinit var docCreate: ActivityResultLauncher<String>

    private val docTreeSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            it?.grantReadWritePermission(contentResolver)
            resultAndFinish(it)
        }

    private val docSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.grantReadWritePermission(contentResolver)
            resultAndFinish(it)
        }

    private fun resultAndFinish(uri: Uri?) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(KEY_REQUEST_DATA, requestData)
            data = uri
        })
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    resultAndFinish(list.getOrNull(0)?.toUri())
                }
            }

            REQUEST_CODE_SAVE_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileDir = FilePickerManager.obtainData().getOrNull(0)
                    if (fileDir == null) {
                        toast(R.string.path_is_empty)
                    } else {
                        if (FileUtils.saveFile(
                                fileDir + "/${reqSaveFile.fileName}",
                                reqSaveFile.fileBytes!!
                            )
                        ) toast(R.string.save_success)
                        else
                            toast(getString(R.string.file_save_failed, ""))

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

        requestData = intent.getParcelableExtra(KEY_REQUEST_DATA)!!

        if (requestData is RequestSaveFile) {
            val permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ), 1
                )

            docCreate = FileUtils.registerResultCreateDocument(
                this,
                reqSaveFile.fileMime
            ) {
                resultAndFinish(null)
                reqSaveFile.fileBytes
            }
        }

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
        when (requestData) {
            is RequestSaveFile -> {
                val binder = intent.getBinder()
                if (binder is ByteArrayBinder) {
                    reqSaveFile.fileBytes = binder.data
                    saveFile()
                }
            }

            is RequestSelectFile -> selectFile()
            is RequestSelectDir -> selectDir()
        }
    }

    private fun saveFile() {
        if (useSystem)
            kotlin.runCatching {
                docCreate.launch(reqSaveFile.fileName)
            }.onFailure {
                it.printStackTrace()
                toast(R.string.sys_doc_picker_error)
                useSystem = false
                return saveFile()
            }
        else {
            pickerDir(REQUEST_CODE_SAVE_FILE)
        }
    }

    private fun selectFile() {
        if (useSystem) {
            kotlin.runCatching {
                docSelector.launch(reqSelectFile.fileMimes.toTypedArray())
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
                .filter(object : AbstractFileFilter() {
                    override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                        return ArrayList(listData.filter { item ->
                            item.isDir || reqSelectFile.fileMimes.contains(File(item.filePath).mimeType)
                        })
                    }
                })
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


    interface IRequestData : Parcelable {}

    @Parcelize
    data class RequestSaveFile(
        val fileName: String = "ttsrv-file.json",
        val fileMime: String = "text/*",

        // 大数据使用Binder传递 这里只是负责临时存取
        @IgnoredOnParcel
        @Suppress("ArrayInDataClass")
        var fileBytes: ByteArray? = null
    ) : IRequestData

    @Parcelize
    data class RequestSelectDir(val rootUri: Uri = Uri.EMPTY) : IRequestData

    @Parcelize
    data class RequestSelectFile(val fileMimes: List<String> = listOf("")) : IRequestData
}