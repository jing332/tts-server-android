package com.github.jing332.tts_server_android.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.compose.widgets.AppSelectionDialog
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.constant.FilePickerMode
import com.github.jing332.tts_server_android.help.ByteArrayBinder
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils
import com.github.jing332.tts_server_android.utils.FileUtils.mimeType
import com.github.jing332.tts_server_android.utils.getBinder
import com.github.jing332.tts_server_android.utils.grantReadWritePermission
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                finish()
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

    private fun checkPermission(permission: String): Boolean {
        val extPermission = ActivityCompat.checkSelfPermission(
            this, permission
        )
        if (extPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    permission,
                ), 1
            )
            return false
        }

        return true
    }

    private var useSystem = false

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lp = window.attributes
        lp.alpha = 0.0f
        window.attributes = lp

        requestData = intent.getParcelableExtra(KEY_REQUEST_DATA)!!

        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (requestData is RequestSaveFile) {
            val permission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ), 1
                )

            docCreate =
                registerForActivityResult(ActivityResultContracts.CreateDocument(reqSaveFile.fileMime)) { uri ->
                    if (uri == null) {
                        finish()
                        return@registerForActivityResult
                    }
                    uri.grantReadWritePermission(contentResolver)
                    lifecycleScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            contentResolver.openOutputStream(uri)
                                .use { it?.write(reqSaveFile.fileBytes) }
                            toast(R.string.save_success)
                        }.onFailure {
                            displayErrorDialog(it)
                        }.onSuccess {
                            withMain { finish() }
                        }
                    }
                }
        }

        var showPromptDialog by mutableStateOf(false)
        setContent {
            AppTheme {
                if (showPromptDialog)
                    AppSelectionDialog(
                        onDismissRequest = {
                            showPromptDialog = false
                            finish()
                        },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileOpen, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(id = R.string.file_picker))
                            }
                        },
                        value = Unit,
                        values = listOf(0, 1),
                        entries = listOf(
                            stringResource(id = R.string.file_picker_mode_system),
                            stringResource(id = R.string.file_picker_mode_builtin)
                        ),
                        onClick = { index, _ ->
                            showPromptDialog = false
                            useSystem = index == 0
                            doAction()
                        }
                    )
            }
        }

        when (AppConfig.filePickerMode.value) {
            FilePickerMode.PROMPT -> {
                showPromptDialog = true
            }

            FilePickerMode.SYSTEM -> {
                useSystem = true
                doAction()
            }

            FilePickerMode.BUILTIN -> {
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
    data class RequestSelectFile(val fileMimes: List<String> = listOf("*")) : IRequestData
}