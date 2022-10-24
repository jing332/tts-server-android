package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.ui.widget.WaitDialog
import com.github.jing332.tts_server_android.utils.runOnUI

class TtsConfigFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {
    companion object {
        const val TAG = "TtsConfigFragment"
    }

    private val binding: FragmentTtsConfigBinding by lazy {
        FragmentTtsConfigBinding.inflate(
            layoutInflater
        )
    }
    private val model: TtsConfigFragmentViewModel by viewModels()

    private val spinnerApiAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerLanguageAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceStyleAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceRoleAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerFormatAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.spinnerApi.adapter = spinnerApiAdapter
        binding.spinnerLanguage.adapter = spinnerLanguageAdapter
        binding.spinnerVoice.adapter = spinnerVoiceAdapter
        binding.spinnerVoiceStyle.adapter = spinnerVoiceStyleAdapter

        /* 长按设置风格强度 */
        binding.spinnerVoiceStyle.setOnLongClickListener {
            if (model.apiLiveData.value?.position == TtsApiType.EDGE) {
                return@setOnLongClickListener true
            }
            AlertDialog.Builder(requireContext()).apply {
                val linear = LinearLayout(context)
                linear.orientation = LinearLayout.VERTICAL
                val tv = TextView(context)
                tv.setPadding(50, 20, 50, 0)
                linear.addView(tv)

                val seekbar = SeekBar(context)
                seekbar.max = 200
                linear.addView(seekbar)
                seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    @SuppressLint("SetTextI18n")
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        tv.text = "风格强度：${(progress * 0.01).toFloat()}"
                        if (progress == 0) seekbar.progress = 1
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        if (model.voiceStyleDegreeLiveData.value != seekbar.progress) {
                            model.voiceStyleDegreeChanged(seekbar.progress)
                            binding.btnApplyChanges.isEnabled = true
                        }
                    }
                })
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

        binding.spinnerApi.onItemSelectedListener = this
        binding.spinnerLanguage.onItemSelectedListener = this
        binding.spinnerVoice.onItemSelectedListener = this
        binding.spinnerVoiceStyle.onItemSelectedListener = this
        binding.spinnerVoiceRole.onItemSelectedListener = this
        binding.spinnerFormat.onItemSelectedListener = this

        binding.btnOpenTtsConfig.setOnClickListener(this)
        binding.btnApplyChanges.setOnClickListener(this)
        binding.btnTest.setOnClickListener(this)

        binding.seekBarRate.setOnSeekBarChangeListener(this)
        binding.seekBarVolume.setOnSeekBarChangeListener(this)
        binding.switchSplitSentences.setOnCheckedChangeListener { _, isChecked ->
            binding.btnApplyChanges.isEnabled = isChecked != model.isSplitSentencesLiveData.value
            model.isSplitSentencesChanged(isChecked)
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
            binding.seekBarVolume.progress = it
        }
        /* 语速 */
        model.rateLiveData.observe(this) {
            Log.d(TAG, "rate:$it")
            binding.seekBarRate.progress = it
        }

        /* 是否分割长段 */
        model.isSplitSentencesLiveData.observe(this) {
            Log.d(TAG, "isSplitSentences $it")
            binding.switchSplitSentences.isChecked = it
        }

        /* 加载数据并更新列表 */
        model.loadData(this.requireContext())
    }

    private var isInit = 0

    /* Spinner选择变更 */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (isInit >= 2)
            binding.btnApplyChanges.isEnabled = true
        when (parent?.id) {
            R.id.spinner_api -> {
                binding.tvStyleDegree.isVisible = position != TtsApiType.EDGE
                val waitDialog = WaitDialog(requireContext())
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
        when (v?.id) {
            R.id.btn_openTtsConfig -> {
                val intent = Intent("com.android.settings.TTS_SETTINGS")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this.startActivity(intent)
            }
            R.id.btn_apply_changes -> {
                v.isEnabled = false
                model.saveConfig(requireContext())
                requireContext().sendBroadcast(Intent(SystemTtsService.ACTION_ON_CONFIG_CHANGED))
            }
            R.id.btn_test -> {
                v.isEnabled = false
                binding.btnApplyChanges.callOnClick()
                model.speakTest { runOnUI { v.isEnabled = true } }
            }
        }
    }

    private fun buildSpinnerAdapter(): ArrayAdapter<String> {
        return ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateSpinner(spinner: Spinner, data: TtsConfigFragmentViewModel.SpinnerData) {
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

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.seekBar_volume -> {
                binding.tvCurrentVolume.text = "${progress - 50}%"
            }
            R.id.seekBar_rate -> {
                if (progress == 0)
                    binding.rateValue.text = "跟随系统或朗读APP"
                else
                    binding.rateValue.text = "${(progress - 50) * 2}%"
            }
        }

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        binding.btnApplyChanges.isEnabled = true
        when (seekBar?.id) {
            R.id.seekBar_volume -> {
                model.volumeChanged(seekBar.progress)
            }
            R.id.seekBar_rate -> {
                model.rateChanged(seekBar.progress)
            }
        }
    }

}