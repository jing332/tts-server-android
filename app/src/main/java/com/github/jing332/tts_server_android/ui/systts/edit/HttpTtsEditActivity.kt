package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsHttpEditActivityBinding
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.adapter.initAccessibilityDelegate
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.SoftKeyboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("DEPRECATION")
class HttpTtsEditActivity : BaseTtsEditActivity<HttpTTS>({ HttpTTS() }) {
    private val vm: HttpTtsEditViewModel by viewModels()
    private val binding: SysttsHttpEditActivityBinding by lazy {
        SysttsHttpEditActivityBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 获取数据 为null表示Add

        binding.apply {
            baseEdit.setData(systemTts)
            binding.numEdit.setData(tts)

            etUrl.setText(tts.url)
            etHeaders.setText(tts.header)
            tvSampleRate.setText(tts.audioFormat.sampleRate.toString())
            checkBoxNeedDecode.isChecked = tts.audioFormat.isNeedDecode
        }

        binding.tilTest.initAccessibilityDelegate()
        binding.tilTest.setEndIconOnClickListener { doTest() }
        binding.etTestText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_GO) {
                doTest()
                true
            } else
                false
        }

        binding.tilUrl.initAccessibilityDelegate()
        // url 帮助按钮
        binding.tilUrl.setEndIconOnClickListener {
            val tv = TextView(this)
            tv.setTextIsSelectable(true)
            tv.text =
                Html.fromHtml(resources.openRawResource(R.raw.help_http_tts_url).readAllText())
            tv.setPadding(20, 20, 20, 20)
            MaterialAlertDialogBuilder(this).setTitle(R.string.help).setView(tv)
                .show()

        }

        binding.tilHeader.initAccessibilityDelegate()
        // 请求头 帮助按钮
        binding.tilHeader.setEndIconOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle(R.string.systts_http_request_header)
                .setMessage(
                    resources.openRawResource(R.raw.help_http_tts_request_header).readAllText()
                )
                .show()
        }

        binding.tilSampleRate.initAccessibilityDelegate()
        // 采样率帮助按钮
        binding.tilSampleRate.setStartIconOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle(R.string.systts_sample_rate)
                .setMessage(R.string.systts_help_sample_rate).show()
        }

        if (AppConfig.testSampleText.isNotEmpty())
            binding.etTestText.setText(AppConfig.testSampleText)

        // 采样率
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(resources.getStringArray(R.array.sample_rate_list).toList())
        binding.tvSampleRate.setAdapter(adapter)
        // 不过滤
        binding.tvSampleRate.threshold = Int.MAX_VALUE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.etTestText.text.toString() != getString(R.string.systts_sample_test_text))
            AppConfig.testSampleText = binding.etTestText.text.toString()
    }

    private fun doTest() {
        updateValueToTts()
        val waitDialog = WaitDialog(this).apply { show() }
        vm.doTest(
            tts,
            binding.etTestText.text.toString(),
            { size, sampleRate, mime, contentType ->
                waitDialog.dismiss()
                MaterialAlertDialogBuilder(this@HttpTtsEditActivity)
                    .setTitle(R.string.systts_test_success)
                    .setMessage(
                        getString(
                            R.string.systts_http_test_msg,
                            size / 1024,
                            mime,
                            sampleRate,
                            contentType
                        )
                    ).setOnDismissListener {
                        vm.stopPlay()
                    }
                    .show()

            },
            { err ->
                waitDialog.dismiss()
                MaterialAlertDialogBuilder(this@HttpTtsEditActivity).setTitle(R.string.test_failed)
                    .setMessage(err.message ?: err.cause?.message)

                    .show()
            })
    }

    override fun onSave() {
        if (binding.etUrl.text?.isEmpty() == true) {
            binding.etUrl.error = getString(R.string.cannot_empty)
            binding.etUrl.requestFocus()
            return
        } else if (binding.baseEdit.checkDisplayNameEmpty()) {
            return
        }

        updateValueToTts()
        SoftKeyboardUtils.hideSoftKeyboard(this)

        super.onSave()
    }


    private fun updateValueToTts() {
        systemTts.let {
            (it.tts as HttpTTS).apply {
                url = binding.etUrl.text.toString()
                header = binding.etHeaders.text.toString()
                audioFormat.sampleRate = binding.tvSampleRate.text.toString().toInt()
                audioFormat.isNeedDecode = binding.checkBoxNeedDecode.isChecked
            }
        }
    }
}