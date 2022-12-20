package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsMsEditActivityBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.setFadeAnim
import kotlinx.coroutines.launch

class MsTtsEditActivity : BackActivity() {
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

        // 帮助 二级语言
        binding.tilSecondaryLocale.setStartIconOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.systts_secondaryLocale)
                .setMessage(R.string.systts_help_secondary_locale).setFadeAnim().show()
        }

        // 接口加载回调
        vm.setCallback(object : MsTtsEditViewModel.CallBack {
            override fun onStart(@MsTtsApiType api: Int) {
                waitDialog.show()
                binding.numEditView.setFormatByApi(api)
            }

            override fun onDone(ret: Result<Unit>) {
                waitDialog.dismiss()
                ret.onFailure { e ->
                    AlertDialog.Builder(this@MsTtsEditActivity)
                        .setTitle(R.string.systts_voice_data_load_failed)
                        .setMessage(e.toString())
                        .setPositiveButton(R.string.retry) { _, _ -> vm.reloadApiData() }
                        .setNegativeButton(R.string.exit) { _, _ -> finish() }
                        .setCancelable(false)
                        .setFadeAnim()
                        .show()
                }
            }
        })

        lifecycleScope.launch {
            // 初始化 注册监听
            vm.init(
                listOf(
                    Pair(getString(R.string.systts_api_edge), R.drawable.ms_edge),
                    Pair(getString(R.string.systts_api_azure), R.drawable.ms_azure),
                    Pair(getString(R.string.systts_api_creation), R.drawable.ic_ms_speech_studio)
                )
            )


            var data: SystemTts? = intent.getParcelableExtra(KEY_DATA)
            if (data == null) data = SystemTts(tts = MsTTS())
            vm.initUserData(data)

            // 组
            appDb.systemTtsDao.allGroup.let { list ->
                binding.baseInfoEditView.setData(data, list)
            }

            // 自动同步数据
            binding.numEditView.setData(data.tts as MsTTS)
        }

        binding.tilTest.setEndIconOnClickListener {
            waitDialog.show()
            val text = binding.etTestText.text.toString()
            vm.doTest(text, { kb ->
                waitDialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle(R.string.systts_test_success)
                    .setMessage("大小: ${kb}KiB")
                    .setOnDismissListener { vm.stopPlay() }
                    .setFadeAnim()
                    .show()
            }, { err ->
                waitDialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle(R.string.test_failed)
                    .setMessage(err.message)
                    .setFadeAnim()
                    .show()
            })

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_systts_config_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.systts_config_edit_save -> {
                setResult(RESULT_OK, Intent().apply { putExtra(KEY_DATA, vm.getData()) })
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}