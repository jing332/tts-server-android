package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.AzureVoiceBean
import com.github.jing332.tts_server_android.bean.CreationVoiceBean
import com.github.jing332.tts_server_android.bean.EdgeVoiceBean
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.ExpressAs
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.service.systts.help.TtsAudioFormat
import com.github.jing332.tts_server_android.service.systts.help.TtsFormatManger
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

class TtsConfigEditViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsSettingsViewModel"
    }

    val displayNameLiveData: MutableLiveData<String> by lazy { MutableLiveData() }
    val readAloudTargetLiveData: MutableLiveData<SpinnerData> by lazy { MutableLiveData() }
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

    private lateinit var mTtsCfgItem: SysTtsConfigItem
    private val mVoiceProperty by lazy { mTtsCfgItem.voiceProperty }

    @OptIn(ExperimentalSerializationApi::class)
    private val mJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private var mCacheDir: String = ""
    private lateinit var mEdgeVoices: List<EdgeVoiceBean>
    private lateinit var mAzureVoices: List<AzureVoiceBean>
    private lateinit var mCreationVoices: List<CreationVoiceBean>

    fun getTtsConfigItem(inputDisplayName: String): SysTtsConfigItem {
        mTtsCfgItem.uiData.apply {
            val voice = voiceLiveData.value?.selected()?.displayName.toString()
            val style = voiceStyleLiveData.value?.selected()?.displayName
            val role = voiceRoleLiveData.value?.selected()?.displayName
            val rate = mTtsCfgItem.voiceProperty.prosody.rate
            val volume = mTtsCfgItem.voiceProperty.prosody.volume
            displayName = inputDisplayName.ifEmpty { voice }
            content = "$style-$role | 语速：$rate% | 音量：$volume%"
        }

        return mTtsCfgItem
    }

    fun loadData(context: Context, ttsConfigItem: SysTtsConfigItem) {
        Log.d(TAG, "loadData")
        this.mTtsCfgItem = ttsConfigItem

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

        displayNameLiveData.value = ttsConfigItem.uiData.displayName
        apiLiveData.value = SpinnerData(apiListData, ttsConfigItem.voiceProperty.api)

        val readAloudTargetList = arrayListOf<SpinnerItemData>()
        arrayOf("全部(默认)", "旁白(解说词)", "对白(人物对话)").forEach {
            readAloudTargetList.add(SpinnerItemData(it, ""))
        }
        readAloudTargetLiveData.value =
            SpinnerData(readAloudTargetList, mTtsCfgItem.readAloudTarget)

        ttsConfigItem.voiceProperty.apply {
            voiceStyleDegreeLiveData.value = (expressAs?.styleDegree?.times(100))?.toInt() ?: 100
            volumeLiveData.value = prosody.volume + 50
            rateLiveData.value = prosody.rate + 100
        }
        mCacheDir = context.cacheDir.path
    }

    fun onReadAloudTargetSelected(position: Int) {
        Log.d(TAG, "onReadAloudTargetSelected: $position")
        mTtsCfgItem.readAloudTarget = position
        /*apiLiveData.value?.position = mTtsCfgItem.api
        apiLiveData.value = apiLiveData.value

        volumeLiveData.value = mTtsCfgItem.volume
        rateLiveData.value = mTtsCfgItem.rate*/
    }

    /* {接口}选中变更 */
    @OptIn(DelicateCoroutinesApi::class)
    fun apiSelected(position: Int, finally: () -> Unit) {
        Log.d(TAG, "apiSelected: $position")
        mTtsCfgItem.voiceProperty.api = position
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
        mTtsCfgItem.locale = languageLiveData.value!!.list[position].value
        val tmpVoiceList = arrayListOf<SpinnerItemData>()
        when (mTtsCfgItem.voiceProperty.api) {
            TtsApiType.EDGE -> {
                mEdgeVoices.forEach { item ->
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
                mAzureVoices.forEach {
                    if (it.locale == mTtsCfgItem.locale)
                        tmpVoiceList.add(
                            SpinnerItemData(
                                it.localName + "（${it.shortName}）",
                                it.shortName
                            )
                        )
                }
            }
            TtsApiType.CREATION -> {
                mCreationVoices.forEach {
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
            if (itemData.value == mVoiceProperty?.voiceName) {
                selectedPos = index
            }
        }
        voiceLiveData.value = SpinnerData(tmpVoiceList, selectedPos)
    }

    /* {声音}选中已变更，更新风格和角色*/
    fun voiceSelected(position: Int) {
        voiceLiveData.value?.also {
            it.position = position
            mVoiceProperty?.voiceName = it.list[position].value
        }
        Log.d(TAG, "voiceSelected ${mVoiceProperty?.voiceName}")

        when (mTtsCfgItem.voiceProperty.api) {
            TtsApiType.AZURE -> {
                mAzureVoices.forEach { voiceItem ->
                    if (mVoiceProperty?.voiceName == voiceItem.shortName) {
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
                                if (mVoiceProperty.expressAs?.style == styleName)
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
                                if (mVoiceProperty.expressAs?.role == roleName)
                                    selectedRole = index + 1
                            }
                        }
                        voiceRoleLiveData.value = SpinnerData(roleList, selectedRole)
                        return
                    }
                }
            }
            TtsApiType.CREATION -> {
                mCreationVoices.forEach { voiceItem ->
                    if (mVoiceProperty.voiceName == voiceItem.shortName) {
                        mVoiceProperty.voiceId = voiceItem.id
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
                                    if (mVoiceProperty.expressAs?.style == styleName)
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
                                    if (mVoiceProperty.expressAs?.role == roleName)
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
        mTtsCfgItem.format = value
        Log.d(TAG, "formatSelected $value")
    }

    fun volumeChanged(progress: Int) {
//        volumeLiveData.value = progress - 50
        mVoiceProperty.prosody.volume = progress - 50
    }

    fun rateChanged(progress: Int) {
//        rateLiveData.value = progress - 100
        mVoiceProperty.prosody.rate = progress - 100
    }

//    fun isSplitSentencesChanged(isChecked: Boolean) {
//        mTtsCfgItem.isSplitSentences = isChecked
//    }

    private fun useEdgeApi() {
        if (!this::mEdgeVoices.isInitialized) {
            /* 使用本地缓存或远程下载 */
            val cachePath = "$mCacheDir/edge/voices.json"
            val data: ByteArray
            if (FileUtils.fileExists(cachePath)) {
                data = File(cachePath).readBytes()
            } else {
                data = Tts_server_lib.getEdgeVoices()
                FileUtils.saveFile(cachePath, data)
            }
            mEdgeVoices = mJson.decodeFromString(data.decodeToString())
        }

        val tmpLangList = arrayListOf<SpinnerItemData>()
        mEdgeVoices.forEach { item ->
            for (it in tmpLangList)
                if (it.value == item.locale) return@forEach

            tmpLangList.add(SpinnerItemData(CnLocalMap.getLanguage(item.locale), item.locale))
        }
        tmpLangList.sortBy { it.value }
        var selected = 0
        tmpLangList.forEachIndexed { index, item ->
            if (mTtsCfgItem.locale == item.value)
                selected = index
        }
        languageLiveData.postValue(SpinnerData(tmpLangList, selected))
        /* Edge接口不支持风格和角色，故设为无 */
        voiceStyleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
        voiceRoleLiveData.postValue(SpinnerData(arrayListOf(SpinnerItemData("无", "")), 0))
    }

    private fun useAzureApi() {
        val cacheFilepath = "$mCacheDir/azure/voices.json"
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

        mAzureVoices = mJson.decodeFromString(data.decodeToString())
        val languageList = arrayListOf<String>()
        mAzureVoices.forEach {
            if (!languageList.contains(it.locale)) {
                languageList.add(it.locale)
            }
        }
        languageList.sort()

        val dataList = arrayListOf<SpinnerItemData>()
        var selected = 0
        languageList.forEachIndexed { i, v ->
            if (mTtsCfgItem.locale == v) { /* 选中配置文件中的位置 */
                selected = i
            }
            dataList.add(SpinnerItemData(CnLocalMap.getLanguage(v), v))
        }
        languageLiveData.postValue(SpinnerData(dataList, selected))
    }

    private fun useCreationApi() {
        val cacheFilepath = "$mCacheDir/creation/voices.json"
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
        mCreationVoices =
            mJson.decodeFromString(data.decodeToString())
        val tmpLanguageList = arrayListOf<String>()
        mCreationVoices.forEach {
            if (!tmpLanguageList.contains(it.locale)) {
                tmpLanguageList.add(it.locale)
            }
        }
        tmpLanguageList.sort()

        val dataList = arrayListOf<SpinnerItemData>()
        var selected = 0
        tmpLanguageList.forEachIndexed { i, v ->
            if (mTtsCfgItem.locale == v) { /* 选中配置文件中的位置 */
                selected = i
            }
            dataList.add(SpinnerItemData(CnLocalMap.getLanguage(v), v))
        }
        languageLiveData.postValue(SpinnerData(dataList, selected))
    }

//    fun saveConfig() {
//        mTtsCfgItem.save()
//    }

    /* 根据API更新音频格式 */
    private fun updateFormatLiveData() {
        val api = when (mTtsCfgItem.voiceProperty.api) {
            0 -> TtsAudioFormat.SupportedApi.EDGE
            1 -> TtsAudioFormat.SupportedApi.AZURE
            else -> TtsAudioFormat.SupportedApi.CREATION //2
        }
        val formats = TtsFormatManger.getFormatsBySupportedApi(api)
        var selected = 0
        val tmpFormats = arrayListOf<SpinnerItemData>()
        formats.forEachIndexed { index, v ->
            if (mTtsCfgItem.format == v) {
                selected = index
            }
            tmpFormats.add(SpinnerItemData(v, v))
        }
        audioFormatLiveData.postValue(SpinnerData(tmpFormats, selected))
    }

    fun voiceStyleSelected(position: Int) {
        voiceStyleLiveData.value?.position = position
        if (mVoiceProperty.expressAs == null) mVoiceProperty.expressAs = ExpressAs()
        mVoiceProperty.expressAs?.style = voiceStyleLiveData.value!!.list[position].value
    }

    fun voiceRoleSelected(position: Int) {
        voiceRoleLiveData.value?.position = position
        if (mVoiceProperty.expressAs == null) mVoiceProperty.expressAs = ExpressAs()
        mVoiceProperty.expressAs?.role = voiceRoleLiveData.value!!.list[position].value
    }

    fun voiceStyleDegreeChanged(progress: Int) {
        if (mVoiceProperty.expressAs == null) mVoiceProperty.expressAs = ExpressAs()
        voiceStyleDegreeLiveData.value = progress
        mVoiceProperty.expressAs?.styleDegree = progress.toFloat() * 0.01F
    }

    data class SpinnerData(var list: List<SpinnerItemData>, var position: Int) {
        fun selected(): SpinnerItemData {
            return list[position]
        }
    }

    data class SpinnerItemData(var displayName: String, var value: String)

    /* 开始朗读测试 */
//    fun speakTest(finally: () -> Unit) {
//        var tts: TextToSpeech? = null
//        tts = TextToSpeech(App.context, {
//            tts?.speak(
//                "如果喜欢这个项目的话请点个Star吧",
//                TextToSpeech.QUEUE_FLUSH,
//                null,
//                Random().nextInt().toString()
//            )
//        }, "com.github.jing332.tts_server_android")
//        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
//            override fun onStart(utteranceId: String?) {}
//            override fun onDone(utteranceId: String?) {
//                finally.invoke()
//            }
//
//            @Deprecated("Deprecated in Java", ReplaceWith("finally.invoke()"))
//            override fun onError(utteranceId: String?) {
//                finally.invoke()
//            }
//
//            override fun onStop(utteranceId: String?, interrupted: Boolean) {
//                super.onStop(utteranceId, interrupted)
//                finally.invoke()
//            }
//        })
//    }

}