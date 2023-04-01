package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditActivityBinding
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PluginTtsEditActivity : BaseTtsEditActivity<PluginTTS>({ PluginTTS() }) {
    companion object {
        const val ACTION_FINISH = "ACTION_FINISH"
    }

    private val engine: TtsPluginUiEngine by lazy { vm.engine }
    private val vm: PluginTtsEditViewModel by viewModels()
    private val binding by lazy {
        SysttsPluginEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val mReceiver by lazy { MyReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root)

        lifecycleScope.launch(Dispatchers.Main) {
            vm.tts = tts
            kotlin.runCatching {
                waitDialog.show()
                withIO { engine.onLoadData() }
                engine.onLoadUI(this@PluginTtsEditActivity, binding.container)
            }.onFailure {
                if (!this@PluginTtsEditActivity.isFinishing)
                    displayErrorDialog(it)
            }
            binding.paramsEdit.setData(tts)
            vm.init()
            waitDialog.dismiss()
        }

        vm.errMessageLiveData.observe(this) {
            displayErrorDialog(it)
        }

        App.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_FINISH))
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
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
                displayErrorDialog(
                    it, getString(
                        R.string.plugin_tts_get_sample_rate_fail_msg,
                        ""
                    )
                )

            }.onSuccess {
                super.onSave()
            }
            waitDialog.dismiss()
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }
}