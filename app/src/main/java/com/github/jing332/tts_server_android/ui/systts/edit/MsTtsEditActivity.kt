package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.databinding.SysttsMsEditActivityBinding
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.custom.adapter.initAccessibilityDelegate
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MsTtsEditActivity : BaseTtsEditActivity<MsTTS>() {
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
        setContentView(binding.root)

        binding.tilApi.initAccessibilityDelegate()
        binding.tilLocale.initAccessibilityDelegate()
        binding.tilVoice.initAccessibilityDelegate()
        binding.tilSecondaryLocale.initAccessibilityDelegate()
        binding.tilStyle.initAccessibilityDelegate()
        binding.tilRole.initAccessibilityDelegate()
        binding.tilTest.initAccessibilityDelegate()

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
                        .setCancelable(false)

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
                Pair(getString(R.string.systts_api_edge), R.drawable.ms_edge),
                Pair(getString(R.string.systts_api_azure), R.drawable.ms_azure),
                Pair(getString(R.string.systts_api_creation), R.drawable.ic_ms_speech_studio)
            )
        )


//        var data: SystemTts? = intent.getParcelableExtra(KEY_DATA)
//        if (data == null) data = SystemTts(tts = MsTTS())
        vm.initUserData(systemTts)

        // 自动同步数据
        binding.baseInfoEditView.setData(systemTts)
        binding.editView.setData(tts)

        // 监听Enter
        binding.etTestText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                doTest()
                return@setOnEditorActionListener true
            }
            false
        }

        if (AppConfig.testSampleText.isNotEmpty())
            binding.etTestText.setText(AppConfig.testSampleText)

        binding.tilTest.setEndIconOnClickListener { doTest() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.etTestText.text.toString() != getString(R.string.systts_sample_test_text))
            AppConfig.testSampleText = binding.etTestText.text.toString()
    }

    private fun doTest() {
        waitDialog.show()
        val text = binding.etTestText.text.toString()
        vm.doTest(text, { kb ->
            waitDialog.dismiss()
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.systts_test_success)
                .setMessage(getString(R.string.systts_test_success_info, kb))
                .setOnDismissListener { vm.stopPlay() }

                .show()
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