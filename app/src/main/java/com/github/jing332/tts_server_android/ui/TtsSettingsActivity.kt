package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.service.tts.TtsConfig
import com.github.jing332.tts_server_android.service.tts.TtsFormatManger
import com.github.jing332.tts_server_android.service.tts.TtsOutputFormat
import com.github.jing332.tts_server_android.service.tts.data.CreationVoicesItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import tts_server_lib.*


class TtsSettingsActivity : AppCompatActivity() {
    companion object {
        const val TAG = "TtsActivity"
        const val ACTION_ON_CONFIG_CHANGED = "action_on_config_changed"
    }

    private lateinit var binding: ActivityTtsSettingsBinding

    private val spinnerApiAdapter: ArrayAdapter<String> by lazy {
        val array = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1
        )
        array.addAll(arrayListOf("edge", "azure", "creation"))
        return@lazy array
    }
    private val spinnerLanguageAdapter: ArrayAdapter<String> by lazy {
        return@lazy ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1
        )
    }
    private val spinnerVoiceAdapter: ArrayAdapter<String> by lazy {
        return@lazy ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1
        )
    }
    private val spinnerFormatAdapter: ArrayAdapter<String> by lazy {
        return@lazy ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1
        )
    }

    lateinit var voicesData: List<CreationVoicesItem> /* 全部数据 */
    val currentVoices = arrayListOf<CreationVoicesItem>() /* 当前语言的voice列表 */
    lateinit var ttsConfig: TtsConfig


    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTtsSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ttsConfig = TtsConfig().loadConfig(this)
        binding.seekBarVolume.progress = ttsConfig.volume
        binding.tvCurrentVolume.text = ttsConfig.volumeToPctString()

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvCurrentVolume.text =
                    "${binding.seekBarVolume.progress - 50}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        /* {保存更改} 按钮 */
        binding.btnSave.setOnClickListener {
            val item = currentVoices[binding.spinnerVoice.selectedItemPosition]
            ttsConfig.api = binding.spinnerApi.selectedItemPosition
            ttsConfig.voiceName = item.shortName
            ttsConfig.voiceId = item.id
            ttsConfig.format = binding.spinnerForamt.selectedItem.toString()
            ttsConfig.volume = binding.seekBarVolume.progress
            ttsConfig.locale = binding.spinnerLanguage.selectedItem.toString()
            ttsConfig.writeConfig(this)
            // 广播更改消息到service以重新加载配置
            sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        }

        /* {TTS设置} 按钮 */
        binding.btnOpenTtsSettings.setOnClickListener {
            openTtsSettings()
        }

        binding.spinnerApi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    TtsOutputFormat.API_EDGE -> {
                        GlobalScope.launch {
                            val data =  Tts_server_lib.getEdgeVoices()

                        }
                    }
                    TtsOutputFormat.API_AZURE -> {

                    }
                    TtsOutputFormat.API_CREATION -> {

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        /* 语言选择变动 */
        binding.spinnerLanguage.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?, view: View?, i: Int, l: Long
                ) {
                    val locale = binding.spinnerLanguage.selectedItem.toString()
                    spinnerVoiceAdapter.clear()
                    currentVoices.clear()
                    voicesData.forEach { item ->
                        if (locale == item.locale) {
                            currentVoices.add(item)
                        }
                    }
                    currentVoices.sortBy { return@sortBy it.shortName }
                    for ((index, v) in currentVoices.withIndex()) {
                        spinnerVoiceAdapter.add(v.shortName)
                        if (ttsConfig.voiceName == v.shortName) {
                            binding.spinnerVoice.setSelection(index)
                        }
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }


        /* 接口 */
        binding.spinnerApi.adapter = spinnerApiAdapter
        /* 语言 */
        binding.spinnerLanguage.adapter = spinnerLanguageAdapter
        /* 发音人 */
        binding.spinnerVoice.adapter = spinnerVoiceAdapter
        /* 音频格式 */
        spinnerFormatAdapter.addAll(TtsFormatManger.getFormatsBySupportedApi(ttsConfig.api))
        binding.spinnerForamt.adapter = spinnerFormatAdapter

        val tmpLanguageList = arrayListOf<String>()
        GlobalScope.launch {
            try {
                var data: ByteArray
                if (File("${cacheDir}/creation_voices.json").exists()) { /* 从缓存中读取 */
                    data = File("${cacheDir}/creation_voices.json").readBytes()
                } else { /* 从微软服务器获取 */
                    data = Tts_server_lib.getCreationVoices()
                    File("${cacheDir}/creation_voices.json").writeBytes(data)
                }

                voicesData = Json { ignoreUnknownKeys = true }.decodeFromString(
                    String(data)
                )

                voicesData.forEach { item ->
                    if (!tmpLanguageList.contains(item.locale)) {
                        tmpLanguageList.add(item.locale)
                    }

                }
                tmpLanguageList.sort()
                runOnUiThread {
                    for ((i, v) in tmpLanguageList.withIndex()) {
                        spinnerLanguageAdapter.add(v)
                        if (ttsConfig.locale == v) { /* 设置选中 */
                            binding.spinnerLanguage.setSelection(i)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateConfigUI() {
//        binding.spinnerLanguage.forEach { view ->  }
//        for (spinnerLanguageAdapter.count)
        for (i in 0..spinnerLanguageAdapter.count) {
            spinnerLanguageAdapter.getItem(i)
        }
    }

    /* 打开系统TTS设置 */
    private fun openTtsSettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.action = "com.android.settings.TTS_SETTINGS"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }
}

