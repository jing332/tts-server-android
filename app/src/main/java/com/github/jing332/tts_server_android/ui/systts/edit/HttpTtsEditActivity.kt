package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_EDIT
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.ActivityHttpTtsEditBinding
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.util.SoftKeyboardUtils

@Suppress("DEPRECATION")
class HttpTtsEditActivity : BackActivity() {
    private val viewModel: HttpTtsEditViewModel by viewModels()
    private val binding by lazy { ActivityHttpTtsEditBinding.inflate(layoutInflater) }

    private var resultCode: Int = RESULT_EDIT
    private var data: SysTts? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.spinnerSampleRate.setSelection(1)

        // 获取数据 为null表示Add
        data = intent.getParcelableExtra<SysTts>(KEY_DATA)
        if (data == null) {
            data = SysTts(tts = HttpTTS())
            resultCode = RESULT_ADD
        }
        data?.let {
            val tts = it.ttsAs<HttpTTS>()
            binding.etName.setText(it.displayName)
            binding.etUrl.setText(tts.url)

            binding.spinnerSampleRate.setSelection(
                viewModel.toSampleRateIndex(
                    tts.audioFormat.sampleRate,
                    this
                )
            )
            binding.checkBoxNeedDecode.isChecked = tts.audioFormat.isNeedDecode
        }

//        binding.etUrl.setText("http://tsn.baidu.com/text2audio,{\"method\": \"POST\", \"body\": \"tex={{java.encodeURI(java.encodeURI(speakText))}}&spd={{(speakSpeed + 5) / 10 + 4}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=6&pit=5&res_tag=audio\"}")
        binding.btnTest.setOnClickListener {
            it.isEnabled = false
            val url =
                binding.etUrl.text.toString()
            viewModel.doTest(
                url,
                binding.etTestText.text.toString(),
                { size, sampleRate, mime, contentType ->
                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试成功")
                        .setMessage(
                            "音频大小：${size / 1024}KB \n格式: $mime " +
                                    "\n采样率：${sampleRate}hz \nContent-Type：$contentType"
                        )
                        .show()
                    it.isEnabled = true
                },
                { err ->
                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试失败").setMessage(err)
                        .show()
                    binding.btnTest.isEnabled = true
                })
        }

        binding.textInputLayoutUrl.setEndIconOnClickListener {
            val htmlStr = "格式：与阅读APP网络TTS引擎相同： " +
                    "<br><i><u>http://请求地址, {\"method\":\"POST\", \"body\": \"POST原始请求体, 也可为GET的url参数。支持使用 {{js代码或变量}} \"}</i></u>" +
                    "<br><br>内置变量：" +
                    "<br> - 文本：<b>{{speakText}}</b>" +
                    "<br> - 语速：<b>{{speakSpeed}}</b>" +
                    "<br> - 音量：<b>{{speakVolume}}</b>" +
                    "<br><br> 示例：" +
                    """<br><i> http://tsn.baidu.com/text2audio,{"method": "POST", "body": "tex={{encodeURI(speakText)}}&spd={{speakSpeed}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol={{speakVolume}}&aue=6&pit=5&res_tag=audio"}  </i>"""

            val tv = TextView(this)
            tv.setTextIsSelectable(true)
            tv.text = Html.fromHtml(htmlStr)
            tv.setPadding(20, 20, 20, 20)
            AlertDialog.Builder(this).setTitle("帮助").setView(tv).show()
        }
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

                data?.let {
                    it.displayName = binding.etName.text.toString()
                    it.ttsAs<HttpTTS>().apply {
                        url = binding.etUrl.text.toString()
                        audioFormat.sampleRate =
                            binding.spinnerSampleRate.selectedItem.toString().toInt()
                        audioFormat.isNeedDecode = binding.checkBoxNeedDecode.isChecked
                    }
                }
                SoftKeyboardUtils.hideSoftKeyboard(this)
                val intent = Intent()
                intent.putExtra(KEY_DATA, data)
                setResult(resultCode, intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}