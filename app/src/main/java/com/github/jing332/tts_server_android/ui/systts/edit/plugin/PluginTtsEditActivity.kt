package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.os.Bundle
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditActivityBinding
import com.github.jing332.tts_server_android.help.plugin.EditUiJsEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PluginTtsEditActivity : BaseTtsEditActivity<PluginTTS>({ PluginTTS() }) {
    private val engine: EditUiJsEngine by lazy { EditUiJsEngine(tts.plugin) }
    private val vm: PluginTtsEditViewModel by viewModels()
    private val binding by lazy {
        SysttsPluginEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root)

        binding.paramsEdit.setData(tts)

        if (basicEditView.displayName.isEmpty()) {
            basicEditView.displayName = tts.plugin.name
        }

        kotlin.runCatching { engine.onLoadUI(binding.root) }.onFailure {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error)
                .setMessage("加载UI时失败：${it.stackTraceToString()}")
                .show()
        }

        vm.errMessageLiveData.observe(this) { e ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error)
                .setMessage("JS执行失败：${e.stackTraceToString()}")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        vm.init(tts)
    }

    override fun onSave() {
        kotlin.runCatching {
            tts.audioFormat.sampleRate = engine.getSampleRate() ?: 16000
        }.onFailure {
            MaterialAlertDialogBuilder(this)
                .setTitle("错误")
                .setMessage("获取音频采样率失败：${it.stackTraceToString()}")
                .show()
        }.onSuccess {
            super.onSave()
        }
    }
}