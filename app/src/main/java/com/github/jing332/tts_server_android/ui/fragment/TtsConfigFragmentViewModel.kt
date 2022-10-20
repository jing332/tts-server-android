package com.github.jing332.tts_server_android.ui.fragment

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.data.CreationVoicesItem
import com.github.jing332.tts_server_android.service.tts.data.EdgeVoicesItem
import com.github.jing332.tts_server_android.service.tts.help.TtsAudioFormat
import com.github.jing332.tts_server_android.service.tts.help.TtsConfig
import com.github.jing332.tts_server_android.service.tts.help.TtsFormatManger
import com.github.jing332.tts_server_android.utils.FileUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tts_server_lib.Tts_server_lib
import java.io.File

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsSettingsViewModel"
    }

    val apiLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val languageLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceStyleLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceRoleLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val audioFormatLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val volumeLiveData: MutableLiveData<Int> by lazy { MutableLiveData() }
    val isSplitSentencesLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    private lateinit var ttsConfig: TtsConfig
    private val json = Json { ignoreUnknownKeys = true }

    var cacheDir: String = ""
    lateinit var creationVoices: List<CreationVoicesItem>
    lateinit var edgeVoices: List<EdgeVoicesItem>

    fun loadData(context: Context) {
        Log.d(TAG, "loadData")
        ttsConfig = TtsConfig().loadConfig(context)

        val apiListData = arrayListOf<SpinnerItemData>()
        context.apply {
            arrayOf(
                getString(R.string.api_edge),
                getString(R.string.api_azure),
                getString(R.string.api_creation)
            ).forEach {
                apiListData.add(SpinnerItemData(it, ""))
            }
        }
        apiLiveData.value = SpinnerData(apiListData, ttsConfig.api)
        volumeLiveData.value = ttsConfig.volume
        isSplitSentencesLiveData.value = ttsConfig.isSplitSentences

        cacheDir = context.cacheDir.path
    }

    /* {接口}选中变更 */
    @OptIn(DelicateCoroutinesApi::class)
    fun apiSelected(position: Int, finally: () -> Unit) {
        ttsConfig.api = position
        updateFormatLiveData()
        GlobalScope.launch {
            when (position) {
                TtsApiType.EDGE -> {
                    useEdgeApi()
                }
                TtsApiType.AZURE -> {

                }
                TtsApiType.CREATION -> {
                    useCreationApi()
                }
            }
            finally.invoke()
        }
    }

    /* {语言}选中已变更, 更新声音列表*/
    fun languageSelected(position: Int) {
        Log.d(TAG, "languageSelected: $position")
        languageLiveData.value?.position = position
        ttsConfig.locale = languageLiveData.value!!.list[position].value
        val tmpVoiceList = arrayListOf<SpinnerItemData>()
        when (ttsConfig.api) {
            TtsApiType.EDGE -> {
                edgeVoices.forEach { item ->
                    if (item.locale == languageLiveData.value!!.list[position].value)
                        tmpVoiceList.add(SpinnerItemData(item.shortName, item.shortName))
                }
            }
            TtsApiType.CREATION -> {
                creationVoices.forEach {
                    if (it.locale == languageLiveData.value!!.list[position].value)
                        tmpVoiceList.add(SpinnerItemData(it.properties.localName, it.shortName))
                }
            }
        }
        tmpVoiceList.sortBy { it.displayName }
        var selectedPos = 0
        tmpVoiceList.forEachIndexed { index, itemData ->
            if (itemData.value == ttsConfig.voiceName) {
                selectedPos = index
            }
        }
        voiceLiveData.value = SpinnerData(tmpVoiceList, selectedPos)
    }

    fun voiceSelected(position: Int) {
        voiceLiveData.value?.also {
            it.position = position
            ttsConfig.voiceName = it.list[position].value
        }
        Log.d(TAG, "voiceSelected ${ttsConfig.voiceName}")

        if (ttsConfig.api == TtsApiType.CREATION) {
            creationVoices.forEach { voiceItem ->
                if (ttsConfig.voiceName == voiceItem.shortName) {
                    ttsConfig.voiceId = voiceItem.id
                    /* 风格 */
                    val styleList = arrayListOf(SpinnerItemData("无", ""))
                    var selectedStyle = 0
                    if (voiceItem.properties.voiceStyleNames.isNotBlank())
                        voiceItem.properties.voiceStyleNames.split(",").apply {
                            styleList[0] = SpinnerItemData("默认", "")
                            forEachIndexed { index, styleName ->
                                if (styleName != "Default")
                                    styleList.add(SpinnerItemData(styleName, styleName))
                                if (ttsConfig.voiceStyle == styleName)
                                    selectedStyle = index + 1
                            }
                        }

                    voiceStyleLiveData.value = SpinnerData(styleList, selectedStyle)

                    /* 角色 */
                    val roleList = arrayListOf(SpinnerItemData("无", ""))
                    var selectedRole = 0
                    if (voiceItem.properties.voiceRoleNames.isNotBlank())
                        voiceItem.properties.voiceRoleNames.split(",").apply {
                            roleList[0] = SpinnerItemData("默认", "")
                            forEachIndexed { index, roleName ->
                                if (roleName != "Default")
                                    roleList.add(SpinnerItemData(roleName, roleName))
                                if (ttsConfig.voiceRole == roleName)
                                    selectedRole = index + 1

                            }
                        }
                    voiceRoleLiveData.value = SpinnerData(roleList, selectedRole)
                    return
                }
            }
        }
    }

    fun formatSelected(position: Int) {
        val value = audioFormatLiveData.value!!.list[position].displayName
        ttsConfig.format = value
        Log.d(TAG, "formatSelected $value")
    }

    fun volumeChanged(volume: Int) {
        volumeLiveData.value = volume
        ttsConfig.volume = volume
    }

    fun isSplitSentencesChanged(isChecked: Boolean) {
        ttsConfig.isSplitSentences = isChecked
    }

    private fun useEdgeApi() {
        ttsConfig.api = TtsApiType.EDGE
        if (!this::edgeVoices.isInitialized) {
            /* 使用本地缓存或远程下载 */
            val cachePath = "$cacheDir/edge/voices.json"
            val data: ByteArray
            if (FileUtils.fileExists(cachePath)) {
                data = File(cachePath).readBytes()
            } else {
                data = Tts_server_lib.getEdgeVoices()
                FileUtils.saveFile(cachePath, data)
            }
            edgeVoices = json.decodeFromString(data.decodeToString())
        }

        val tmpLangList = arrayListOf<SpinnerItemData>()
        edgeVoices.forEach { item ->
            for (it in tmpLangList)
                if (it.value == item.locale) return@forEach

            tmpLangList.add(SpinnerItemData(item.locale, item.locale))
        }
        tmpLangList.sortBy { it.displayName }
        var selected = 0
        tmpLangList.forEachIndexed { index, item ->
            if (ttsConfig.locale == item.value)
                selected = index
        }
        languageLiveData.postValue(SpinnerData(tmpLangList, selected))
        /* Edge接口不支持风格和角色，故设为无 */
        voiceStyleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
        voiceRoleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
    }

    private fun useCreationApi() {
        ttsConfig.api = TtsApiType.CREATION
        val cacheFilepath = "$cacheDir/creation/voices.json"
        val data: ByteArray
        if (FileUtils.fileExists(cacheFilepath)) {
            data = File(cacheFilepath).readBytes()
        } else {
            try {
                data = Tts_server_lib.getCreationVoices()
                FileUtils.saveFile(cacheFilepath, data)
            } catch (e: Exception) {
                Log.e(TAG, "获取Creation Voices数据失败")
                e.printStackTrace()
                return
            }
        }
        creationVoices =
            json.decodeFromString(data.decodeToString())
        val tmpLanguageList = arrayListOf<String>()
        creationVoices.forEach {
            if (!tmpLanguageList.contains(it.locale)) {
                tmpLanguageList.add(it.locale)
            }
        }
        tmpLanguageList.sort()

        val dataList = arrayListOf<SpinnerItemData>()
        var selected = 0
        for ((i, v) in tmpLanguageList.withIndex()) {
            if (ttsConfig.locale == v) { /* 选中配置文件中的位置 */
                selected = i
            }
            dataList.add(SpinnerItemData(v, v))
        }
        languageLiveData.postValue(SpinnerData(dataList, selected))
    }

    fun saveConfig(context: Context) {
        ttsConfig.writeConfig(context)
    }

    /* 根据API更新音频格式 */
    private fun updateFormatLiveData() {
        val api = when (ttsConfig.api) {
            0 -> TtsAudioFormat.SupportedApi.EDGE
            1 -> TtsAudioFormat.SupportedApi.AZURE
            else -> TtsAudioFormat.SupportedApi.CREATION //2
        }
        val formats = TtsFormatManger.getFormatsBySupportedApi(api)
        var selected = 0
        val tmpFormats = arrayListOf<SpinnerItemData>()
        formats.forEachIndexed { index, v ->
            if (ttsConfig.format == v) {
                selected = index
            }
            tmpFormats.add(SpinnerItemData(v, v))
        }
        audioFormatLiveData.postValue(SpinnerData(tmpFormats, selected))
    }

    fun voiceStyleSelected(position: Int) {
        ttsConfig.voiceStyle = voiceStyleLiveData.value!!.list[position].value
    }

    fun voiceROleSelected(position: Int) {
        ttsConfig.voiceRole = voiceRoleLiveData.value!!.list[position].value
    }

    class SpinnerData(var list: List<SpinnerItemData>, var position: Int) {

    }

    class SpinnerItemData(var displayName: String, var value: String)
}