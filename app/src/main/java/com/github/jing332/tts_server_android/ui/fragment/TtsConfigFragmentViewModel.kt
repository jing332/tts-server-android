package com.github.jing332.tts_server_android.ui.fragment

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.AzureVoiceBean
import com.github.jing332.tts_server_android.bean.CreationVoiceBean
import com.github.jing332.tts_server_android.bean.EdgeVoiceBean
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.help.TtsAudioFormat
import com.github.jing332.tts_server_android.service.tts.help.TtsConfig
import com.github.jing332.tts_server_android.service.tts.help.TtsFormatManger
import com.github.jing332.tts_server_android.util.FileUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tts_server_lib.Tts_server_lib
import java.io.File
import java.util.*

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsSettingsViewModel"
    }

    val apiLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val languageLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceStyleLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val voiceStyleDegreeLiveData: MutableLiveData<Int> by lazy { MutableLiveData() }
    val voiceRoleLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val audioFormatLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
    val volumeLiveData: MutableLiveData<Int> by lazy { MutableLiveData() }
    val rateLiveData: MutableLiveData<Int> by lazy { MutableLiveData() }
    val isSplitSentencesLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    private lateinit var ttsConfig: TtsConfig

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private var cacheDir: String = ""
    private lateinit var edgeVoices: List<EdgeVoiceBean>
    private lateinit var azureVoices: List<AzureVoiceBean>
    private lateinit var creationVoices: List<CreationVoiceBean>

    fun loadData(context: Context) {
        Log.d(TAG, "loadData")
        ttsConfig = TtsConfig.read()

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
        apiLiveData.value = SpinnerData(apiListData, ttsConfig.list[0].api)
        voiceStyleDegreeLiveData.value = ttsConfig.list[0].voiceStyleDegree
        volumeLiveData.value = ttsConfig.list[0].volume
        rateLiveData.value = ttsConfig.list[0].rate
        isSplitSentencesLiveData.value = ttsConfig.isSplitSentences

        cacheDir = context.cacheDir.path
    }

    /* {接口}选中变更 */
    @OptIn(DelicateCoroutinesApi::class)
    fun apiSelected(position: Int, finally: () -> Unit) {
        ttsConfig.list[0].api = position
        apiLiveData.value?.position = position
        updateFormatLiveData()
        GlobalScope.launch {
            when (position) {
                TtsApiType.EDGE -> useEdgeApi()
                TtsApiType.AZURE -> useAzureApi()
                TtsApiType.CREATION -> useCreationApi()
            }
            finally.invoke()
        }
    }

    /* {语言}选中已变更, 更新声音列表*/
    fun languageSelected(position: Int) {
        Log.d(TAG, "languageSelected: $position")
        languageLiveData.value?.position = position
        ttsConfig.list[0].locale = languageLiveData.value!!.list[position].value
        val tmpVoiceList = arrayListOf<SpinnerItemData>()
        when (ttsConfig.list[0].api) {
            TtsApiType.EDGE -> {
                edgeVoices.forEach { item ->
                    if (item.locale == languageLiveData.value!!.list[position].value)
                        tmpVoiceList.add(
                            SpinnerItemData(
                                CnLocalMap.getEdgeVoice(item.shortName) + "（${item.shortName}）",
                                item.shortName
                            )
                        )
                }
            }
            TtsApiType.AZURE -> {
                azureVoices.forEach {
                    if (it.locale == ttsConfig.list[0].locale)
                        tmpVoiceList.add(
                            SpinnerItemData(
                                it.localName + "（${it.shortName}）",
                                it.shortName
                            )
                        )
                }
            }
            TtsApiType.CREATION -> {
                creationVoices.forEach {
                    if (it.locale == languageLiveData.value!!.list[position].value)
                        tmpVoiceList.add(
                            SpinnerItemData(
                                it.properties.localName + "（${it.shortName}）",
                                it.shortName
                            )
                        )
                }
            }
        }
        tmpVoiceList.sortBy { it.displayName }
        var selectedPos = 0
        tmpVoiceList.forEachIndexed { index, itemData ->
            if (itemData.value == ttsConfig.list[0].voiceName) {
                selectedPos = index
            }
        }
        voiceLiveData.value = SpinnerData(tmpVoiceList, selectedPos)
    }

    /* {声音}选中已变更，更新风格和角色*/
    fun voiceSelected(position: Int) {
        voiceLiveData.value?.also {
            it.position = position
            ttsConfig.list[0].voiceName = it.list[position].value
        }
        Log.d(TAG, "voiceSelected ${ttsConfig.list[0].voiceName}")

        when (ttsConfig.list[0].api) {
            TtsApiType.AZURE -> {
                azureVoices.forEach { voiceItem ->
                    if (ttsConfig.list[0].voiceName == voiceItem.shortName) {
                        /* 风格 */
                        val styleList = arrayListOf(SpinnerItemData("无", ""))
                        var selectedStyle = 0
                        voiceItem.styleList?.let {
                            styleList[0] = SpinnerItemData("默认", "")
                            voiceItem.styleList.forEachIndexed { index, styleName ->
                                styleList.add(
                                    SpinnerItemData(
                                        CnLocalMap.getStyleAndRole(styleName),
                                        styleName
                                    )
                                )
                                if (ttsConfig.list[0].voiceStyle == styleName)
                                    selectedStyle = index + 1
                            }
                        }
                        voiceStyleLiveData.value = SpinnerData(styleList, selectedStyle)

                        /* 角色 */
                        val roleList = arrayListOf(SpinnerItemData("无", ""))
                        var selectedRole = 0
                        voiceItem.rolePlayList?.let {
                            roleList[0] = SpinnerItemData("默认", "")
                            voiceItem.rolePlayList.forEachIndexed { index, roleName ->
                                roleList.add(
                                    SpinnerItemData(
                                        CnLocalMap.getStyleAndRole(roleName),
                                        roleName
                                    )
                                )
                                if (ttsConfig.list[0].voiceRole == roleName)
                                    selectedRole = index + 1
                            }
                        }
                        voiceRoleLiveData.value = SpinnerData(roleList, selectedRole)
                        return
                    }
                }
            }
            TtsApiType.CREATION -> {
                creationVoices.forEach { voiceItem ->
                    if (ttsConfig.list[0].voiceName == voiceItem.shortName) {
                        ttsConfig.list[0].voiceId = voiceItem.id
                        /* 风格 */
                        val styleList = arrayListOf(SpinnerItemData("无", ""))
                        var selectedStyle = 0
                        if (voiceItem.properties.voiceStyleNames.isNotBlank())
                            voiceItem.properties.voiceStyleNames.split(",").apply {
                                styleList[0] = SpinnerItemData("默认", "")
                                forEachIndexed { index, styleName ->
                                    if (styleName != "Default")
                                        styleList.add(
                                            SpinnerItemData(
                                                CnLocalMap.getStyleAndRole(styleName),
                                                styleName
                                            )
                                        )
                                    if (ttsConfig.list[0].voiceStyle == styleName)
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
                                        roleList.add(
                                            SpinnerItemData(
                                                CnLocalMap.getStyleAndRole(roleName),
                                                roleName
                                            )
                                        )
                                    if (ttsConfig.list[0].voiceRole == roleName)
                                        selectedRole = index + 1

                                }
                            }
                        voiceRoleLiveData.value = SpinnerData(roleList, selectedRole)
                        return
                    }
                }
            }

        }

    }

    fun formatSelected(position: Int) {
        val value = audioFormatLiveData.value!!.list[position].displayName
        ttsConfig.list[0].format = value
        Log.d(TAG, "formatSelected $value")
    }

    fun volumeChanged(volume: Int) {
        volumeLiveData.value = volume
        ttsConfig.list[0].volume = volume
    }

    fun rateChanged(rate: Int) {
        rateLiveData.value = rate
        ttsConfig.list[0].rate = rate
    }

    fun isSplitSentencesChanged(isChecked: Boolean) {
        ttsConfig.isSplitSentences = isChecked
    }

    private fun useEdgeApi() {
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

            tmpLangList.add(SpinnerItemData(CnLocalMap.getLanguage(item.locale), item.locale))
        }
        tmpLangList.sortBy { it.value }
        var selected = 0
        tmpLangList.forEachIndexed { index, item ->
            if (ttsConfig.list[0].locale == item.value)
                selected = index
        }
        languageLiveData.postValue(SpinnerData(tmpLangList, selected))
        /* Edge接口不支持风格和角色，故设为无 */
        voiceStyleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
        voiceRoleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
    }

    private fun useAzureApi() {
        val cacheFilepath = "$cacheDir/azure/voices.json"
        val data: ByteArray
        if (FileUtils.fileExists(cacheFilepath)) {
            data = File(cacheFilepath).readBytes()
        } else {
            try {
                data = Tts_server_lib.getAzureVoice()
                FileUtils.saveFile(cacheFilepath, data)
            } catch (e: Exception) {
                Log.e(TAG, "获取Azure Voices数据失败")
                e.printStackTrace()
                return
            }
        }

        azureVoices = json.decodeFromString(data.decodeToString())
        val languageList = arrayListOf<String>()
        azureVoices.forEach {
            if (!languageList.contains(it.locale)) {
                languageList.add(it.locale)
            }
        }
        languageList.sort()

        val dataList = arrayListOf<SpinnerItemData>()
        var selected = 0
        languageList.forEachIndexed { i, v ->
            if (ttsConfig.list[0].locale == v) { /* 选中配置文件中的位置 */
                selected = i
            }
            dataList.add(SpinnerItemData(CnLocalMap.getLanguage(v), v))
        }
        languageLiveData.postValue(SpinnerData(dataList, selected))
    }

    private fun useCreationApi() {
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
        tmpLanguageList.forEachIndexed { i, v ->
            if (ttsConfig.list[0].locale == v) { /* 选中配置文件中的位置 */
                selected = i
            }
            dataList.add(SpinnerItemData(CnLocalMap.getLanguage(v), v))
        }
        languageLiveData.postValue(SpinnerData(dataList, selected))
    }

    fun saveConfig(context: Context) {
        ttsConfig.save()
    }

    /* 根据API更新音频格式 */
    private fun updateFormatLiveData() {
        val api = when (ttsConfig.list[0].api) {
            0 -> TtsAudioFormat.SupportedApi.EDGE
            1 -> TtsAudioFormat.SupportedApi.AZURE
            else -> TtsAudioFormat.SupportedApi.CREATION //2
        }
        val formats = TtsFormatManger.getFormatsBySupportedApi(api)
        var selected = 0
        val tmpFormats = arrayListOf<SpinnerItemData>()
        formats.forEachIndexed { index, v ->
            if (ttsConfig.list[0].format == v) {
                selected = index
            }
            tmpFormats.add(SpinnerItemData(v, v))
        }
        audioFormatLiveData.postValue(SpinnerData(tmpFormats, selected))
    }

    fun voiceStyleSelected(position: Int) {
        ttsConfig.list[0].voiceStyle = voiceStyleLiveData.value!!.list[position].value
    }

    fun voiceRoleSelected(position: Int) {
        ttsConfig.list[0].voiceRole = voiceRoleLiveData.value!!.list[position].value
    }

    fun voiceStyleDegreeChanged(progress: Int) {
        voiceStyleDegreeLiveData.value = progress
        ttsConfig.list[0].voiceStyleDegree = progress
    }

    class SpinnerData(var list: List<SpinnerItemData>, var position: Int)
    class SpinnerItemData(var displayName: String, var value: String)

    /* 开始朗读测试 */
    fun speakTest(finally: () -> Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(App.context, {
            tts?.speak(
                "如果喜欢这个项目的话请点个Star吧",
                TextToSpeech.QUEUE_FLUSH,
                null,
                Random().nextInt().toString()
            )
        }, "com.github.jing332.tts_server_android")
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                finally.invoke()
            }

            @Deprecated("Deprecated in Java", ReplaceWith("finally.invoke()"))
            override fun onError(utteranceId: String?) {
                finally.invoke()
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                super.onStop(utteranceId, interrupted)
                finally.invoke()
            }
        })
    }
}