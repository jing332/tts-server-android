package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_EDIT
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.ActivityHttpTtsEditBinding
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.HttpTtsQuickEditView
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.SoftKeyboardUtils
import com.github.jing332.tts_server_android.util.setFadeAnim

@Suppress("DEPRECATION")
class HttpTtsEditActivity : BackActivity() {
    private val vm: HttpTtsEditViewModel by viewModels()
    private val binding by lazy { ActivityHttpTtsEditBinding.inflate(layoutInflater) }

    private var resultCode: Int = RESULT_EDIT
    private var data: SysTts? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 获取数据 为null表示Add
        data = intent.getParcelableExtra(KEY_DATA)
        if (data == null) {
            data = SysTts(tts = HttpTTS())
            resultCode = RESULT_ADD
        }
        data?.let {
            val tts = it.ttsAs<HttpTTS>()
            binding.apply {
                etName.setText(it.displayName)
                etUrl.setText(tts.url)
                etHeaders.setText(tts.header)
                binding.numEdit.rate = tts.rate
                binding.numEdit.volume = tts.volume

                tvSampleRate.setText(tts.audioFormat.sampleRate.toString())
                checkBoxNeedDecode.isChecked = tts.audioFormat.isNeedDecode
            }
        }

        binding.btnTest.setOnClickListener {
            updateValueToTts()
            val waitDialog = WaitDialog(this).apply { show() }
            vm.doTest(
                data!!.tts as HttpTTS,
                binding.etTestText.text.toString(),
                { size, sampleRate, mime, contentType ->
                    waitDialog.dismiss()
                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试成功")
                        .setMessage(
                            "音频大小：${size / 1024}KB \n格式：$mime " +
                                    "\n采样率：${sampleRate}hz \nContent-Type：$contentType"
                        ).setOnDismissListener {
                            vm.stopPlay()
                        }
                        .setFadeAnim().show()

                },
                { err ->
                    waitDialog.dismiss()

                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试失败").setMessage(err)
                        .setFadeAnim()
                        .show()
                })
        }

        // 帮助按钮
        binding.textInputLayoutUrl.setEndIconOnClickListener {
            val tv = TextView(this)
            tv.setTextIsSelectable(true)
            tv.text =
                Html.fromHtml(resources.openRawResource(R.raw.help_http_tts_url).readAllText())
            tv.setPadding(20, 20, 20, 20)
            AlertDialog.Builder(this).setTitle(R.string.help).setView(tv).setFadeAnim().show()
        }

        binding.numEdit.callBack = object : HttpTtsQuickEditView.CallBack {
            override fun onValueChanged(rate: Int, volume: Int): String {
                data?.tts?.let {
                    val tts = it as HttpTTS
                    it.rate = rate
                    it.volume = volume

                    val result = AnalyzeUrl(
                        mUrl = tts.url,
                        speakText = binding.etTestText.text.toString(),
                        speakSpeed = rate,
                        speakVolume = volume
                    ).eval()
                    return result.body ?: "解析url失败"
                }
                return ""
            }
        }

        // 采样率
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(resources.getStringArray(R.array.sample_rate_list).toList())
        binding.tvSampleRate.setAdapter(adapter)
        // 不过滤
        binding.tvSampleRate.threshold = Int.MAX_VALUE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_http_tts_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                if (binding.etName.text?.isBlank() == true) {
                    binding.etName.error = getString(R.string.cannot_empty)
                    binding.etName.requestFocus()
                    return super.onOptionsItemSelected(item)
                } else if (binding.etUrl.text?.isEmpty() == true) {
                    binding.etUrl.error = getString(R.string.cannot_empty)
                    binding.etUrl.requestFocus()
                    return super.onOptionsItemSelected(item)
                }

                updateValueToTts()
                SoftKeyboardUtils.hideSoftKeyboard(this)
                val intent = Intent()
                intent.putExtra(KEY_DATA, data)
                setResult(resultCode, intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateValueToTts() {
        data?.let {
            it.displayName = binding.etName.text.toString()
            it.ttsAs<HttpTTS>().apply {
                url = binding.etUrl.text.toString()
                header = binding.etHeaders.text.toString()
                audioFormat.sampleRate = binding.tvSampleRate.text.toString().toInt()
                audioFormat.isNeedDecode = binding.checkBoxNeedDecode.isChecked
            }
        }
    }
}