package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_EDIT
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.ActivityMsTtsEditBinding
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog
import com.github.jing332.tts_server_android.util.setFadeAnim

class MsTtsEditActivity : BackActivity() {
    companion object {
        const val TAG = "MsTtsEditActivity"
    }

    private val binding: ActivityMsTtsEditBinding by lazy {
        ActivityMsTtsEditBinding.inflate(layoutInflater).apply { m = vm }
    }
    private val vm: MsTtsEditViewModel2 by viewModels()

    private var resultCode = RESULT_EDIT

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 帮助 二级语言
        binding.tilSecondaryLocale.setStartIconOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.secondaryLocale)
                .setMessage(R.string.help_secondary_locale).setFadeAnim().show()
        }

        // 朗读目标切换
        binding.radioGroupRaTarget.setOnCheckedChangeListener { _, id ->
            val pos = when (id) {
                R.id.radioBtn_ra_all -> ReadAloudTarget.ALL
                R.id.radioBtn_only_ra_aside -> ReadAloudTarget.ASIDE
                R.id.radioBtn_only_ra_dialogue -> ReadAloudTarget.DIALOGUE
                else -> return@setOnCheckedChangeListener
            }
            vm.raTargetChanged(pos)
        }

        // 监听朗读目标
        vm.raTargetLiveData.observe(this) {
            binding.radioGroupRaTarget.apply {
                children.forEach { (it as RadioButton).isChecked = false }
                (getChildAt(it) as RadioButton).isChecked = true
            }
        }

        val waitDialog = WaitDialog(this)
        // 接口加载回调
        vm.setCallback(object : MsTtsEditViewModel2.CallBack {
            override fun onStart(@MsTtsApiType api: Int) {
                waitDialog.show()
                binding.numEditView.setFormatByApi(api)
            }

            override fun onDone(ret: Result<Unit>) {
                waitDialog.dismiss()
                ret.onFailure { e ->
                    AlertDialog.Builder(this@MsTtsEditActivity)
                        .setTitle(R.string.title_voice_data_failed)
                        .setMessage(e.toString())
                        .setPositiveButton(R.string.retry) { _, _ -> vm.reloadApiData() }
                        .setNegativeButton(R.string.exit) { _, _ -> finish() }
                        .setCancelable(false)
                        .setFadeAnim()
                        .show()
                }
            }
        })

        // 初始化 注册监听
        vm.init(
            listOf(
                Pair(getString(R.string.api_edge), R.drawable.ms_edge),
                Pair(getString(R.string.api_azure), R.drawable.ms_azure),
                Pair(getString(R.string.api_creation), R.drawable.ic_ms_speech_studio)
            )
        )

        var data: SysTts? = intent.getParcelableExtra(KEY_DATA)
        if (data == null) {
            resultCode = RESULT_ADD
            data = SysTts(tts = MsTTS())
        }

        vm.initUserData(data)

        // 自动同步数据
        binding.numEditView.setData(data.tts as MsTTS)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_systts_config_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.systts_config_edit_save -> {
                val data = vm.getData(binding.etName.text.toString())
                setResult(resultCode, Intent().apply { putExtra(KEY_DATA, data) })
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}