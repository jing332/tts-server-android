package com.github.jing332.tts_server_android.ui.systts.edit

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.databinding.SysttsLocalEditActivityBinding
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.runOnIO
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LocalTtsEditActivity : BaseTtsEditActivity<LocalTTS>({ LocalTTS() }) {
    private val vm: LocalTtsViewModel by viewModels()
    private val binding: SysttsLocalEditActivityBinding by lazy {
        SysttsLocalEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val waitDialog by lazy { WaitDialog(this) }

    override fun onSave() {
        vm.checkAndSetDisplayName(binding.basicEdit.displayName)

        super.onSave()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.test.etTestText.text.toString() != getString(R.string.systts_sample_test_text))
            AppConfig.testSampleText = binding.test.etTestText.text.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            basicEdit.setData(systemTts)
            paramsEdit.setData(tts)

            if (AppConfig.testSampleText.isNotEmpty())
                test.etTestText.setText(AppConfig.testSampleText)

            test.tilTest.setEndIconOnClickListener {
                waitDialog.show()
                vm.doTest(test.etTestText.text.toString(), { // init
                    it?.let {
                        waitDialog.dismiss()
                        MaterialAlertDialogBuilder(this@LocalTtsEditActivity)
                            .setTitle(R.string.test_failed)
                            .setMessage(it)
                            .show()
                        return@doTest
                    }
                }, { // start
                    waitDialog.dismiss()
                    MaterialAlertDialogBuilder(this@LocalTtsEditActivity)
                        .setTitle(R.string.systts_test_success)
                        .setMessage(R.string.systts_state_playing)
                        .setOnDismissListener {
                            vm.stopTestPlay()
                        }
                        .show()
                }, { // finished

                })

            }
        }

        vm.init(systemTts, { // onStart
            waitDialog.show()
        }, { //onDone
            waitDialog.dismiss()
        })

        lifecycleScope.runOnIO {
            if (appDb.systemTtsDao.allTts.find { it.tts is LocalTTS } == null) {
                withMain {
                    MaterialAlertDialogBuilder(this@LocalTtsEditActivity)
                        .setTitle(R.string.warning)
                        .setMessage("请注意！【语言】以及【声音】选项并非全部TTS都支持，有些虽说可以读取显示但实际上是 无效/不可用的。")
                        .show()
                }
            }
        }
    }
}