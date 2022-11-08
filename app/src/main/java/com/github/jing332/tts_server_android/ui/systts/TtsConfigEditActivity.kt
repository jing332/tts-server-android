package com.github.jing332.tts_server_android.ui.systts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.databinding.ActivityTtsConfigEditBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.custom.widget.WaitDialog

class TtsConfigEditActivity : BackActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, ConvenientSeekbar.OnSeekBarChangeListener {

    companion object {
        const val TAG = "TtsConfigEditActivity"
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
    }

    private val binding: ActivityTtsConfigEditBinding by lazy {
        ActivityTtsConfigEditBinding.inflate(layoutInflater)
    }
    private val model: TtsConfigEditViewModel by viewModels()

    private val spinnerRaTargetAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerApiAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerLanguageAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceStyleAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceRoleAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerFormatAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }

    /* -1表示添加 */
    private var position = -1

    @Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        position = intent.getIntExtra(KEY_POSITION, -1)

        binding.spinnerReadAloudTarget.adapter = spinnerRaTargetAdapter
        binding.spinnerApi.adapter = spinnerApiAdapter
        binding.spinnerLanguage.adapter = spinnerLanguageAdapter
        binding.spinnerVoice.adapter = spinnerVoiceAdapter
        binding.spinnerVoiceStyle.adapter = spinnerVoiceStyleAdapter

        /* 长按设置风格强度 */
        binding.spinnerVoiceStyle.setOnLongClickListener {
            if (model.apiLiveData.value?.position == TtsApiType.EDGE) {
                return@setOnLongClickListener true
            }
            AlertDialog.Builder(this).apply {
                val linear = LinearLayout(context)
                linear.orientation = LinearLayout.VERTICAL
                val tv = TextView(context)
                tv.setPadding(50, 20, 50, 0)
                linear.addView(tv)

                val seekbar = ConvenientSeekbar(context)
                seekbar.max = 200
                linear.addView(seekbar)
                seekbar.onSeekBarChangeListener = object : ConvenientSeekbar.OnSeekBarChangeListener {
                    @SuppressLint("SetTextI18n")
                    override fun onProgressChanged(
                        seekBar: ConvenientSeekbar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        tv.text = "风格强度：${(progress * 0.01).toFloat()}"
                        if (progress == 0) seekbar.progress = 1
                    }

                    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}
                    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
                        if (model.voiceStyleDegreeLiveData.value != seekbar.progress) {
                            model.voiceStyleDegreeChanged(seekbar.progress)
                            binding.btnApplyChanges.isEnabled = true
                        }
                    }
                }
                seekbar.progress = model.voiceStyleDegreeLiveData.value ?: 50
                seekbar.setPadding(50, 20, 50, 50)
                setView(linear).setNeutralButton(getString(R.string.reset)) { _, _ ->
                    model.voiceStyleDegreeChanged(100) /* 重置 */
                    binding.btnApplyChanges.isEnabled = true
                }.create().show()
            }

            true
        }
        binding.spinnerVoiceRole.adapter = spinnerVoiceRoleAdapter
        binding.spinnerFormat.adapter = spinnerFormatAdapter

        binding.spinnerReadAloudTarget.onItemSelectedListener = this
        binding.spinnerApi.onItemSelectedListener = this
        binding.spinnerLanguage.onItemSelectedListener = this
        binding.spinnerVoice.onItemSelectedListener = this
        binding.spinnerVoiceStyle.onItemSelectedListener = this
        binding.spinnerVoiceRole.onItemSelectedListener = this
        binding.spinnerFormat.onItemSelectedListener = this

        binding.btnOpenTtsConfig.setOnClickListener(this)
        binding.btnApplyChanges.setOnClickListener(this)
        binding.btnTest.setOnClickListener(this)

        binding.seekBarRate.onSeekBarChangeListener = this
        binding.seekBarVolume.onSeekBarChangeListener = this
        binding.switchSplitSentences.setOnCheckedChangeListener { _, isChecked ->
            binding.btnApplyChanges.isEnabled = isChecked != model.isSplitSentencesLiveData.value
//            model.isSplitSentencesChanged(isChecked)
        }
        /* 显示名称*/
        model.displayNameLiveData.observe(this) { text ->
            binding.etDisplayName.setText(text)
        }
        /* 朗读目标 */
        model.readAloudTargetLiveData.observe(this) { data ->
            updateSpinner(binding.spinnerReadAloudTarget, data)
        }
        /* 接口列表 */
        model.apiLiveData.observe(this) { data ->
            updateSpinner(binding.spinnerApi, data)
        }
        /* 语言列表 */
        model.languageLiveData.observe(this) { data ->
            Log.d(TAG, "languageList size:${data.list.size}")
            updateSpinner(binding.spinnerLanguage, data)
        }
        /* 声音列表 */
        model.voiceLiveData.observe(this) { data ->
            Log.d(TAG, "voiceList size:${data.list.size}")
            updateSpinner(binding.spinnerVoice, data)
        }
        /* 风格 */
        model.voiceStyleLiveData.observe(this) { data ->
            Log.d(TAG, "styleList size:${data.list.size}")
            updateSpinner(binding.spinnerVoiceStyle, data)
        }
        model.voiceStyleDegreeLiveData.observe(this) { data ->
            Log.d(TAG, "styleDegree :$data")
            binding.tvStyleDegree.text =
                getString(R.string.voice_degree_value, "${(data * 0.01).toFloat()}")
        }
        /* 角色 */
        model.voiceRoleLiveData.observe(this) { data ->
            Log.d(TAG, "roleList size:${data.list.size}")
            updateSpinner(binding.spinnerVoiceRole, data)
        }
        /* 音频格式列表 */
        model.audioFormatLiveData.observe(this) { data ->
            Log.d(TAG, "audioFormatList size:${data.list.size}")
            updateSpinner(binding.spinnerFormat, data)
            spinnerFormatAdapter.clear()
            data.list.forEach {
                spinnerFormatAdapter.add(it.displayName)
            }
            binding.spinnerFormat.setSelection(data.position)
        }
        /* 音量 */
        model.volumeLiveData.observe(this) {
            Log.d(TAG, "volume:$it")
            binding.seekBarVolume.seekBar.progress = it
        }
        /* 语速 */
        model.rateLiveData.observe(this) {
            Log.d(TAG, "rate:$it")
            binding.seekBarRate.seekBar.progress = it
        }
        /* 是否分割长段 */
        model.isSplitSentencesLiveData.observe(this) {
            Log.d(TAG, "isSplitSentences $it")
            binding.switchSplitSentences.isChecked = it
        }

        /* 加载数据并更新列表 */
        var cfg =
            intent.getSerializableExtra(KEY_DATA)?.let { it as SysTtsConfigItem }
        if (cfg == null) cfg = SysTtsConfigItem()
        model.loadData(this, cfg)
    }

    private var isInit = 0

    /* Spinner选择变更 */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (isInit >= 2)
            binding.etDisplayName.setText("")
        when (parent?.id) {
            R.id.spinner_readAloudTarget -> {
                model.onReadAloudTargetSelected(position)
                binding.switchSplitSentences.visibility =
                    if (position == 0) View.VISIBLE else View.INVISIBLE
            }
            R.id.spinner_api -> {
                binding.tvStyleDegree.isVisible = position != TtsApiType.EDGE
                val waitDialog = WaitDialog(this)
                waitDialog.show()
                model.apiSelected(position) { waitDialog.dismiss() }
            }
            R.id.spinner_language -> model.languageSelected(position)
            R.id.spinner_voice -> {
                model.voiceSelected(position)
                isInit++
            }
            R.id.spinner_voiceStyle -> {
                model.voiceStyleSelected(position)
            }
            R.id.spinner_voiceRole -> {
                model.voiceRoleSelected(position)
                isInit++
            }
            R.id.spinner_format -> model.formatSelected(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onClick(v: View?) {
        /*    when (v?.id) {
                R.id.btn_openTtsConfig -> {
                    val intent = Intent("com.android.settings.TTS_SETTINGS")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    this.startActivity(intent)
                }
    //            R.id.btn_apply_changes -> {
    //                v.isEnabled = false
    //                model.saveConfig()
    //                sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
    //            }
                R.id.btn_test -> {
                    v.isEnabled = false
                    binding.btnApplyChanges.callOnClick()
                    model.speakTest { runOnUI { v.isEnabled = true } }
                }
            }*/
    }

    private fun buildSpinnerAdapter(): ArrayAdapter<String> {
        return ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateSpinner(spinner: Spinner, data: TtsConfigEditViewModel.SpinnerData) {
        spinner.also {
            val adapter = it.adapter as ArrayAdapter<String>
            adapter.clear()
            data.list.forEach { v ->
                adapter.add(v.displayName)
            }
            /* 解决position相同时不调用onItemSelectedListener */
            if (it.selectedItemPosition == data.position) {
                val cb = spinner.onItemSelectedListener
                cb?.onItemSelected(
                    spinner,
                    null,
                    data.position,
                    data.position.toLong()
                )
            } else {
                it.setSelection(data.position)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_systts_config_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.systts_config_edit_save -> {

                val intent = Intent()
                intent.putExtra(
                    KEY_DATA,
                    model.getTtsConfigItem(binding.etDisplayName.text.toString())
                )
                intent.putExtra(KEY_POSITION, position)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.seekBar_volume -> {
                binding.tvCurrentVolume.text = "${progress - 50}%"
            }
            R.id.seekBar_rate -> {
                if (progress == 0)
                    binding.rateValue.text = "跟随系统或朗读APP"
                else
                    binding.rateValue.text = "${(progress - 100)}%"
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}
    override fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {
        binding.btnApplyChanges.isEnabled = true
        when (seekBar.id) {
            R.id.seekBar_volume -> {
                model.volumeChanged(seekBar.progress)
            }
            R.id.seekBar_rate -> {
                model.rateChanged(seekBar.progress)
            }
        }
    }


}