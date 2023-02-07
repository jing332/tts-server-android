package com.github.jing332.tts_server_android.ui.systts.edit.local

import android.os.Bundle
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsLocalEditActivityBinding
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LocalTtsEditActivity : BaseTtsEditActivity<LocalTTS>({ LocalTTS() }) {
    private val vm: LocalTtsViewModel by viewModels()
    private val binding: SysttsLocalEditActivityBinding by lazy {
        SysttsLocalEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val waitDialog by lazy { WaitDialog(this) }

    override fun onSave() {
        vm.checkAndSetDisplayName(basicEditView.displayName)
        super.onSave()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (waitDialog.isShowing) waitDialog.dismiss()
    }

    override fun onTest(text: String) {
        waitDialog.show()
        vm.doTest(text, { // 直接播放模式
            waitDialog.dismiss()
            MaterialAlertDialogBuilder(this@LocalTtsEditActivity)
                .setTitle(R.string.systts_test_success)
                .setMessage(R.string.systts_state_synthesizing)
                .setOnDismissListener {
                    vm.stopTestPlay()
                }
                .show()
        }, { audio, sampleRate ->
            waitDialog.dismiss()
            MaterialAlertDialogBuilder(this@LocalTtsEditActivity)
                .setTitle(R.string.systts_test_success)
                .setMessage(
                    getString(R.string.systts_local_test_msg, audio.size / 1024, sampleRate)
                )
                .setOnDismissListener {
                    stopPlay()
                }
                .show()
            playAudio(audio)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root, binding.testLayout.tilTest)

        binding.apply {
            binding.paramsEdit.setData(tts)
        }

        vm.voiceEnabledLiveData.observe(this) {
            binding.tilVoice.isEnabled = it
        }

        vm.init(systemTts, { // onStart
            waitDialog.show()
        }, { //onDone
            waitDialog.dismiss()
            if (!it) {
                MaterialAlertDialogBuilder(this).setTitle("引擎初始化失败").show()
            }
        })
    }
}