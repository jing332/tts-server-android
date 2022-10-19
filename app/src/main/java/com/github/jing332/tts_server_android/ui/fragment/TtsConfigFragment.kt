package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.ui.TtsSettingsActivity
import com.github.jing332.tts_server_android.ui.TtsSettingsViewModel
import com.github.jing332.tts_server_android.ui.widget.WaitDialog

class TtsConfigFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    val binding: FragmentTtsConfigBinding by lazy { FragmentTtsConfigBinding.inflate(layoutInflater) }
    val model: TtsSettingsViewModel by viewModels()


    private val spinnerApiAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerLanguageAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
    private val spinnerVoiceAdapter: ArrayAdapter<String> by lazy { buildSpinnerAdapter() }
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
        binding.spinnerForamt.adapter = spinnerFormatAdapter

        binding.spinnerApi.onItemSelectedListener = this
        binding.spinnerLanguage.onItemSelectedListener = this
        binding.spinnerVoice.onItemSelectedListener = this
        binding.spinnerForamt.onItemSelectedListener = this

        binding.btnOpenTtsConfig.setOnClickListener(this)
        binding.btnApplyChanges.setOnClickListener(this)

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvCurrentVolume.text = "${progress - 50}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                model.volumeChanged(seekBar!!.progress)
                binding.btnApplyChanges.isEnabled = true
            }
        })
        /* 接口列表 */
        model.apiLiveData.observe(this) { data ->
            updateSpinner(binding.spinnerApi, data)
        }
        /* 语言列表 */
        model.languageLiveData.observe(this) { data ->
            Log.d(TtsSettingsActivity.TAG, "languageList size:${data.list.size}")
            updateSpinner(binding.spinnerLanguage, data)
        }
        /* 声音列表 */
        model.voiceLiveData.observe(this) { data ->
            Log.d(TtsSettingsActivity.TAG, "voiceList size:${data.list.size}")
            updateSpinner(binding.spinnerVoice, data)
        }
        /* 音频格式列表 */
        model.audioFormatLiveData.observe(this) { data ->
            Log.d(TtsSettingsActivity.TAG, "audioFormatList size:${data.list.size}")
            updateSpinner(binding.spinnerForamt, data)
            spinnerFormatAdapter.clear()
            data.list.forEach {
                spinnerFormatAdapter.add(it.displayName)
            }
            binding.spinnerForamt.setSelection(data.position)
        }
        /* 音量 */
        model.volumeLiveData.observe(this) {
            Log.d(TtsSettingsActivity.TAG, "volume:$it")
            binding.seekBarVolume.progress = it
        }
        /* 加载数据并更新列表 */
        model.loadData(this.requireContext())

    }


    private var isInit = false

    /* Spinner选择变更 */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        binding.btnApplyChanges.isEnabled = isInit
        when (parent?.id) {
            R.id.spinner_api -> {
                if (position == TtsApiType.AZURE) {
                    binding.spinnerApi.setSelection(0)
                    return
                }
                binding.seekBarVolume.isEnabled = position != TtsApiType.EDGE
                val waitDialog = WaitDialog(requireContext())
                waitDialog.show()
                model.apiSelected(position) { waitDialog.dismiss() }
            }
            R.id.spinner_language -> model.languageSelected(position)
            R.id.spinner_voice -> {
                model.voiceSelected(position)
                isInit = true
            }
            R.id.spinner_foramt -> model.formatSelected(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onClick(v: View?) {
        when (v) {
            binding.btnOpenTtsConfig -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.action = "com.android.settings.TTS_SETTINGS"
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
    private fun updateSpinner(spinner: Spinner, data: TtsSettingsViewModel.SpinnerData) {
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

}