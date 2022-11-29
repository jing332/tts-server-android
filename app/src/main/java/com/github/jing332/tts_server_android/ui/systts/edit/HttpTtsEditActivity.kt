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
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.FileUtils.readText
import com.github.jing332.tts_server_android.util.SoftKeyboardUtils

@Suppress("DEPRECATION")
class HttpTtsEditActivity : BackActivity() {
    private val vm: HttpTtsEditViewModel by viewModels()
    private val vb by lazy { ActivityHttpTtsEditBinding.inflate(layoutInflater) }

    private var resultCode: Int = RESULT_EDIT
    private var data: SysTts? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)

        vb.spinnerSampleRate.setSelection(1)

        // 获取数据 为null表示Add
        data = intent.getParcelableExtra<SysTts>(KEY_DATA)
        if (data == null) {
            data = SysTts(tts = HttpTTS())
            resultCode = RESULT_ADD
        }
        data?.let {
            val tts = it.ttsAs<HttpTTS>()
            vb.apply {
                etName.setText(it.displayName)
                etUrl.setText(tts.url)
                etHeaders.setText(tts.header)

                spinnerSampleRate.setSelection(
                    vm.toSampleRateIndex(
                        tts.audioFormat.sampleRate,
                        this@HttpTtsEditActivity
                    )
                )
                checkBoxNeedDecode.isChecked = tts.audioFormat.isNeedDecode

            }
        }

//        binding.etUrl.setText("http://tsn.baidu.com/text2audio,{\"method\": \"POST\", \"body\": \"tex={{java.encodeURI(java.encodeURI(speakText))}}&spd={{(speakSpeed + 5) / 10 + 4}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=6&pit=5&res_tag=audio\"}")
        vb.btnTest.setOnClickListener {
            val url =
                vb.etUrl.text.toString()
            val waitDialog = WaitDialog(this).apply { show() }
            vm.doTest(
                url,
                vb.etTestText.text.toString(),
                vb.etHeaders.text.toString(),
                { size, sampleRate, mime, contentType ->
                    waitDialog.dismiss()
                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试成功")
                        .setMessage(
                            "音频大小：${size / 1024}KB \n格式：$mime " +
                                    "\n采样率：${sampleRate}hz \nContent-Type：$contentType"
                        )
                        .show()
                },
                { err ->
                    waitDialog.dismiss()
                    AlertDialog.Builder(this@HttpTtsEditActivity).setTitle("测试失败").setMessage(err)
                        .show()
                })
        }

        vb.textInputLayoutUrl.setEndIconOnClickListener {
            val tv = TextView(this)
            tv.setTextIsSelectable(true)
            tv.text = Html.fromHtml(resources.openRawResource(R.raw.help_http_tts_url).readText())
            tv.setPadding(20, 20, 20, 20)
            AlertDialog.Builder(this).setTitle(R.string.help).setView(tv).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_http_tts_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                if (vb.etName.text?.isBlank() == true) {
                    vb.etName.error = getString(R.string.cannot_empty)
                    vb.etName.requestFocus()
                    return super.onOptionsItemSelected(item)
                } else if (vb.etUrl.text?.isEmpty() == true) {
                    vb.etUrl.error = getString(R.string.cannot_empty)
                    vb.etUrl.requestFocus()
                    return super.onOptionsItemSelected(item)
                }

                data?.let {
                    it.displayName = vb.etName.text.toString()
                    it.ttsAs<HttpTTS>().apply {
                        url = vb.etUrl.text.toString()
                        header = vb.etHeaders.text.toString()
                        audioFormat.sampleRate =
                            vb.spinnerSampleRate.selectedItem.toString().toInt()
                        audioFormat.isNeedDecode = vb.checkBoxNeedDecode.isChecked
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