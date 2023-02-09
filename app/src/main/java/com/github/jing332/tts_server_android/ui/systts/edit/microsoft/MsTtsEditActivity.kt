package com.github.jing332.tts_server_android.ui.systts.edit.microsoft

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsEditActivityBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class MsTtsEditActivity : BaseTtsEditActivity<MsTTS>({
    MsTTS(locale = App.locale.run { "$language-$country" })
}) {
    companion object {
        const val TAG = "MsTtsEditActivity"
    }

    private val binding: SysttsMsEditActivityBinding by lazy {
        SysttsMsEditActivityBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val vm: MsTtsEditViewModel by viewModels()

    private val waitDialog by lazy { WaitDialog(this) }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditContentView(binding.root, binding.testLayout.tilTest)

        // 帮助 二级语言
        binding.tilSecondaryLocale.setStartIconOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle(R.string.systts_secondaryLocale)
                .setMessage(R.string.systts_help_secondary_locale).show()
        }

        // 接口加载回调
        vm.setCallback(object : MsTtsEditViewModel.CallBack {
            override fun onStart(@MsTtsApiType api: Int) {
                waitDialog.show()
                binding.editView.setFormatByApi(api)
            }

            override fun onDone(ret: Result<Unit>) {
                waitDialog.dismiss()
                ret.onFailure { e ->
                    MaterialAlertDialogBuilder(this@MsTtsEditActivity)
                        .setTitle(R.string.systts_voice_data_load_failed)
                        .setMessage(e.toString())
                        .setPositiveButton(R.string.retry) { _, _ -> vm.reloadApiData() }
                        .setNegativeButton(R.string.exit) { _, _ -> finish() }
                        .show()
                }
            }
        })

        vm.styleDegreeVisibleLiveData.observe(this) {
            binding.editView.isStyleDegreeVisible = it
        }

        // 初始化 注册监听
        vm.init(
            listOf(
                Pair(getString(R.string.systts_api_edge), R.drawable.ic_ms_edge),
                Pair(getString(R.string.systts_api_azure), R.drawable.ic_microsoft),
                Pair(getString(R.string.systts_api_creation), R.drawable.ic_ms_speech_studio)
            )
        )

        binding.editView.setData(tts)
        vm.initUserData(systemTts)
    }

    override fun onTest(text: String) {
        waitDialog.show()
        vm.doTest(text, { audio, sampleRate, mime ->
            waitDialog.dismiss()


            val s =
                if (tts.format == "webm-24khz-16bit-24kbps-mono-opus") getString(R.string.systts_ms_webm_warn_msg) + "\n" else ""

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.systts_test_success)
                .setMessage(
                    s + getString(
                        R.string.systts_test_success_info,
                        audio.size / 1024, sampleRate, mime
                    )
                )
                .setOnDismissListener { stopPlay() }
                .show()
            playAudio(audio)
        }, { err ->
            waitDialog.dismiss()
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.test_failed)
                .setMessage(err.message)

                .show()
        })
    }

    override fun onSave() {
        vm.onSave()
        super.onSave()
    }
}