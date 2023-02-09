package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditActivityBinding
import com.github.jing332.tts_server_android.help.plugin.EditUiJsEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.util.readableString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PluginTtsEditActivity : BaseTtsEditActivity<PluginTTS>({ PluginTTS() }) {
    private val engine: EditUiJsEngine by lazy { EditUiJsEngine(tts.requirePlugin) }
    private val vm: PluginTtsEditViewModel by viewModels()
    private val binding by lazy {
        SysttsPluginEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root)

        kotlin.runCatching { engine.onLoadUI(this, binding.container) }.onFailure {
            AppDialogs.displayErrorDialog(this, it.stackTraceToString())
        }

        vm.errMessageLiveData.observe(this) {
            AppDialogs.displayErrorDialog(this, it.stackTraceToString())
        }

        binding.paramsEdit.setData(tts)
        vm.init(tts)
    }

    private val waitDialog by lazy { WaitDialog(this) }

    override fun onTest(text: String) {
        waitDialog.show()
        vm.doTest(text,
            { audio, sampleRate, mime ->
                waitDialog.dismiss()
                MaterialAlertDialogBuilder(this@PluginTtsEditActivity)
                    .setTitle(R.string.systts_test_success)
                    .setMessage(
                        getString(
                            R.string.systts_test_success_info,
                            audio.size / 1024,
                            sampleRate,
                            mime
                        )
                    ).setOnDismissListener {
                        stopPlay()
                    }
                    .show()
                playAudio(audio)
            }, { err ->
                waitDialog.dismiss()
                MaterialAlertDialogBuilder(this@PluginTtsEditActivity).setTitle(R.string.test_failed)
                    .setMessage(err.message ?: err.cause?.message)
                    .show()
            })
    }

    override fun onSave() {
        lifecycleScope.launch(Dispatchers.Main) {
            systemTts.displayName = vm.checkDisplayName(basicEditView.displayName)
            kotlin.runCatching {
                waitDialog.show()
                withIO {
                    tts.audioFormat.sampleRate =
                        engine.getSampleRate(tts.locale, tts.voice) ?: 16000
                }
            }.onFailure {
                AppDialogs.displayErrorDialog(
                    this@PluginTtsEditActivity, getString(
                        R.string.plugin_tts_get_sample_rate_fail_msg,
                        it.readableString
                    )
                )
            }.onSuccess {
                super.onSave()
            }
            waitDialog.dismiss()
        }
    }
}