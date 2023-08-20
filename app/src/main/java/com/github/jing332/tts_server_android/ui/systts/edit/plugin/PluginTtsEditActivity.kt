package com.github.jing332.tts_server_android.ui.systts.edit.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditActivityBinding
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
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

    private val tts by lazy { getTts<PluginTTS>() }
    private val engine: TtsPluginUiEngine by lazy { vm.engine }
    private val vm: PluginTtsEditViewModel by viewModels()
    private val binding by lazy {
        SysttsPluginEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val mReceiver by lazy { MyReceiver() }
    private val mWaitDialog by lazy { WaitDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root)

        lifecycleScope.launch(Dispatchers.Main) {
            vm.tts = tts
            kotlin.runCatching {
                mWaitDialog.show()
                withIO { engine.onLoadData() }
                engine.onLoadUI(this@PluginTtsEditActivity, binding.container)
            }.onFailure {
                if (!this@PluginTtsEditActivity.isFinishing)
                    displayErrorDialog(it)
            }
            binding.paramsEdit.setData(tts)
            vm.init()
            mWaitDialog.dismiss()
        }

        vm.errMessageLiveData.observe(this) {
            displayErrorDialog(it)
        }

        AppConst.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_FINISH))
    }

    override fun onDestroy() {
        super.onDestroy()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
    }

    fun displayAudioInfoDialog(size: Int, sampleRate: Int, mime: String) {
        MaterialAlertDialogBuilder(this@PluginTtsEditActivity)
            .setTitle(R.string.systts_test_success)
            .setMessage(
                getString(
                    R.string.systts_test_success_info,
                    size / 1024,
                    sampleRate,
                    mime
                ) + if (sampleRate == 0 && mime.isBlank()) "\n" + getString(R.string.no_audio_data_head_warn) else ""
            )
            .setOnDismissListener { stopPlay() }
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onAudition(text: String) {
        mWaitDialog.show()
        vm.doTest(text,
            { audio, sampleRate, mime ->
                mWaitDialog.dismiss()
                displayAudioInfoDialog(audio.size, sampleRate, mime)
                playAudio(audio)
            }, { err ->
                mWaitDialog.dismiss()
                displayErrorDialog(err, getString(R.string.test_failed))
            }
        )
    }

    override fun onSave() {
        lifecycleScope.launch(Dispatchers.Main) {
            systemTts.displayName = vm.checkDisplayName(basicEditView.displayName)
            mWaitDialog.show()
            kotlin.runCatching {
                withIO {
                    tts.audioFormat.sampleRate =
                        engine.getSampleRate(tts.locale, tts.voice) ?: 16000

                    tts.audioFormat.isNeedDecode = engine.isNeedDecode(tts.locale, tts.voice)
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

            mWaitDialog.dismiss()
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH)
                finish()
        }
    }
}