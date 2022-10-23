package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.ui.widget.WaitDialog

class TtsConfigFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {
    companion object {
        const val TAG = "TtsConfigFragment"
    }

    val binding: FragmentTtsConfigBinding by lazy { FragmentTtsConfigBinding.inflate(layoutInflater) }
    val model: TtsConfigFragmentViewModel by viewModels()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.spinnerApi.adapter = spinnerApiAdapter
        binding.spinnerLanguage.adapter = spinnerLanguageAdapter
        binding.spinnerVoice.adapter = spinnerVoiceAdapter
        binding.spinnerVoiceStyle.adapter = spinnerVoiceStyleAdapter
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
        when (v) {
            binding.btnOpenTtsConfig -> {
                val intent = Intent("com.android.settings.TTS_SETTINGS")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this.startActivity(intent)
            }
            binding.btnApplyChanges -> {
                v.isEnabled = false
                model.saveConfig(requireContext())
                requireContext().sendBroadcast(Intent(SystemTtsService.ACTION_ON_CONFIG_CHANGED))
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