package com.github.jing332.tts_server_android.ui.systts.edit.bgm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.databinding.SysttsBgmEditActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsBgmListItemBinding
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.ExoPlayerActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.util.ASFUriUtils
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class BgmTtsEditActivity : BaseTtsEditActivity<BgmTTS>({ BgmTTS() }) {
    private lateinit var brv: BindingAdapter

    private val binding by lazy { SysttsBgmEditActivityBinding.inflate(layoutInflater) }

    private val mDirSelection =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            if (it == null) return@registerForActivityResult
            kotlin.runCatching {
                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    it, DocumentsContract.getTreeDocumentId(it)
                )
                val path = ASFUriUtils.getPath(this, docUri)!!
                tts.musicList.add(path)
                updateList()
            }.onFailure {
                displayErrorDialog(it, "目录选择失败")
            }
        }

    override fun onSave() {
        systemTts.speechRule.tagRuleId = ""
        systemTts.speechRule.tag = ""
        systemTts.speechRule.target = SpeechTarget.BGM
        super.onSave()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        basicEditView.liteModeEnabled = true
        setEditContentView(binding.root)

        binding.btnAddFolder.clickWithThrottle {
            mDirSelection.launch(Uri.EMPTY)
        }

        brv = binding.rv.linear().setup {
            addType<BgmItemModel>(R.layout.systts_bgm_list_item)

            onCreate {
                itemView.clickWithThrottle {
                    val model: BgmItemModel = getModel()
                    val files = FileUtils.getAllFilesInFolder(File(model.name))
                        .filter { FileUtils.getMimeType(it)?.startsWith("audio") == true }

                    val items = files.map { it.name }.toTypedArray()
                    MaterialAlertDialogBuilder(this@BgmTtsEditActivity)
                        .setTitle("点击播放")
                        .setItems(items) { _, which ->
                            val file = files[which]
                            toast(file.absolutePath)
                            startActivity(
                                Intent(
                                    this@BgmTtsEditActivity,
                                    ExoPlayerActivity::class.java
                                ).apply {
                                    putExtra(ExoPlayerActivity.PARAM_URI, file.absolutePath)
                                })
                        }
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }

                val binding: SysttsBgmListItemBinding = getBinding()
                binding.btnDelete.clickWithThrottle {
                    val model: BgmItemModel = getModel()
                    AppDialogs.displayDeleteDialog(this@BgmTtsEditActivity, model.name) {
                        tts.musicList.remove(model.name)
                        updateList()
                    }

                }
            }
        }.apply { models = mutableListOf<BgmItemModel>() }

        binding.paramsEdit.setData(tts)
        systemTts.speechTarget = SpeechTarget.BGM
        updateList()

        checkFileReadPermission()
    }

    private fun checkFileReadPermission() {
        val permission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.grant_permission_all_file)
                    .setMessage("否则将无法读取音乐文件导致无法播放！")
                    .setPositiveButton(R.string.go_to_settings) { _, _ ->
                        startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    }
                    .setCancelable(false)
                    .setNeutralButton(R.string.exit) { _, _ ->
                        onSave()
                    }
                    .show()
            }
        }*/
    }

    private fun updateList() {
        brv.models = tts.musicList.map { BgmItemModel(it) }
        binding.tvTip.isVisible = tts.musicList.isEmpty()
    }
}