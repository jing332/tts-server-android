package com.github.jing332.tts_server_android.ui.systts.edit.bgm

import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsBgmEditActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsBgmListItemBinding
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.ASFUriUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle

class BgmTtsEditActivity : BaseTtsEditActivity<BgmTTS>({ BgmTTS() }) {
    private lateinit var brv: BindingAdapter

    private val binding by lazy { SysttsBgmEditActivityBinding.inflate(layoutInflater) }

    private val mDirSelection =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            kotlin.runCatching {
                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    it, DocumentsContract.getTreeDocumentId(it)
                )
                val path = ASFUriUtils.getPath(this, docUri)!!
                tts.musicList.add(path)
                updateList()
            }.onFailure {
                AppDialogs.displayErrorDialog(this, "文件目录选择失败：${it.stackTraceToString()}")
            }
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
        systemTts.isBgm = true
        updateList()
    }

    private fun updateList() {
        brv.models = tts.musicList.map { BgmItemModel(it) }
        binding.tvTip.isVisible = tts.musicList.isEmpty()
    }
}