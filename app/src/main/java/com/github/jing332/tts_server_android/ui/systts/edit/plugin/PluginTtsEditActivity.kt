package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.os.Bundle
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditActivityBinding
import com.github.jing332.tts_server_android.help.plugin.JsEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PluginTtsEditActivity : BaseTtsEditActivity<PluginTTS>({ PluginTTS() }) {
    private val binding by lazy { SysttsPluginEditActivityBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (basicEditView.displayName.isEmpty()) {
            basicEditView.displayName = tts.plugin.name
        }
    }

    override fun onSave() {
        val engine = JsEngine(plugin = tts.plugin)

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