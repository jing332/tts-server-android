package com.github.jing332.tts_server_android.ui.systts.edit.microsoft

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.model.speech.tts.ExpressAs
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.ui.systts.edit.SpinnerData
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.runOnIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class MsTtsEditViewModel : ViewModel() {
    companion object {
        const val TAG = "MsTtsEditViewModel"
        private val repo: MsTtsEditRepository by lazy { MsTtsEditRepository() }

        // 风格和角色中忽略此字符串
        private const val IGNORE_VALUE_DEFAULT = "Default"

        // 默认的SpinnerItem 用于风格和角色
        private val DEFAULT_SPINNER_ITEM: SpinnerItem by lazy {
            SpinnerItem(App.context.getString(R.string.default_str), "")
        }
    }

    val styleDegreeVisibleLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var mTts: MsTTS
    private lateinit var mData: SystemTts

    // 全部的list
    private lateinit var mAllVoiceList: List<GeneralVoiceData>

    // 当前地区下的list
    private lateinit var mCurrentVoiceList: List<GeneralVoiceData>

    // UI数据
    var ui: UiData = UiData()

    fun onSave() {
        val displayName = mData.displayName
        if (displayName.isNullOrEmpty() || isAutoGenDisplayName(displayName)) { // 为自动生成的
            mData.displayName = getDisplayName()
        } else { // 为用户自定义
            mData.displayName = displayName
        }
        if (mTts.api == MsTtsApiType.EDGE) {
            mTts.expressAs = null
        }
    }

    // 判断是否为自动生成的名称
    private fun isAutoGenDisplayName(s: String): Boolean {
        return Regex(".*（.*-.*-.*）").replace(s, "").isBlank()
    }

    private fun getDisplayName(): String {
        ui.voices.selectedItem?.value?.let {
            if (it is GeneralVoiceData) return it.localVoiceName + "（${it.voiceName}）"
        }
        return ""
    }

    fun initUserData(data: SystemTts) {
        mData = data
        mTts = data.tts as MsTTS

        ui.apis.position = mTts.api
    }

    interface CallBack {
        // 每次加载语音数据时
        fun onStart(@MsTtsApiType api: Int)

        // 加载完毕
        fun onDone(ret: Result<Unit>)
    }

    private var mCallback: CallBack? = null
    fun setCallback(callBack: CallBack?) {
        mCallback = callBack
    }

    /*
    * 重新加载接口数据
    * */
    fun reloadApiData() {
        viewModelScope.launch(Dispatchers.Default) {
            val spinner = ui.apis
            mTts.api = spinner.position
            withMain { mCallback?.onStart(mTts.api) }

            kotlin.runCatching {
                // 开始获取 当前接口下的所有数据
                mAllVoiceList = repo.voicesByApi(spinner.position)
            }.onFailure {
                it.printStackTrace()
                withMain { mCallback?.onDone(Result.failure(it)) }
                return@launch
            }

            // 更新地区列表
            updateLocales(mAllVoiceList)

            withMain { mCallback?.onDone(Result.success(Unit)) }
        }
    }

    /**
     * 初始化Spinner
     */
    fun init(list: List<Pair<String, Int>>) {
        // 接口
        ui.apis.items = list.map {
            SpinnerItem(
                displayText = it.first, value = it.first, imageResId = it.second
            )
        }

        ui.apis.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) reloadApiData()
        }

        // 地区
        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                mTts.locale = (ui.locales.selectedItem?.value ?: "") as String
                viewModelScope.launch(Dispatchers.Default) { updateVoices(mAllVoiceList) }
            }
        }

        // 语音
        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                viewModelScope.launch(Dispatchers.Default) {
                    ui.voices.selectedItem?.value?.let {
                        if (it is GeneralVoiceData) {
                            mTts.voiceName = it.voiceName
                            mTts.voiceId = it.voiceId
                            updateStyles(it)
                            updateRoles(it)
                            updateSecondaryLocales(it)
                        }
                    }
                }
            }
        }

        // 风格
        ui.styles.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.styles.selectedItem?.value?.let { v ->
                    mTts.expressAs = mTts.expressAs ?: ExpressAs()
                    mTts.expressAs?.let {
                        it.style = v.toString().ifEmpty { null }
                    }
                    return@addOnPropertyChangedCallback
                }
            }
        }

        // 角色
        ui.roles.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.roles.selectedItem?.value?.let { v ->
                    mTts.expressAs = mTts.expressAs ?: ExpressAs()
                    mTts.expressAs?.let {
                        it.role = v.toString().ifEmpty { null }
                        return@addOnPropertyChangedCallback
                    }
                }
            }
        }

        // 二级语言
        ui.secondaryLocales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                ui.secondaryLocales.selectedItem?.value?.let {
                    mTts.secondaryLocale = it.toString()
                    return@addOnPropertyChangedCallback
                }
            }
        }
    }

    // 更新地区列表
    private fun updateLocales(allList: List<GeneralVoiceData>) {
        ui.locales.apply {
            items = allList.map { SpinnerItem(it.localeName, it.locale) }.distinctBy { it.value }
                .sortedBy { it.value.toString() }
            val index = if (mTts.locale.endsWith("-"))
                items.indexOfFirst { it.value.toString().startsWith(mTts.locale) }
            else items.indexOfFirst { it.value.toString() == mTts.locale }
            position = max(0, index)
        }
    }

    // 更新发音列表
    private fun updateVoices(data: List<GeneralVoiceData>) {
        ui.voices.apply {
            // 筛选出当前地区的Voice
            mCurrentVoiceList = data.filter { it.locale == ui.locales.selectedItem?.value }
                .sortedBy { it.voiceName }

            items = mCurrentVoiceList.map {
                SpinnerItem(displayText = "${it.localVoiceName} (${it.voiceName})", value = it)
            }
            val pos = mCurrentVoiceList.indexOfFirst { it.voiceName == mTts.voiceName }
            position = max(pos, 0)
        }
    }

    // 更新风格列表
    private fun updateStyles(currentVoice: GeneralVoiceData) {
        ui.styles.apply {
            reset()
            currentVoice.localStyleList?.let { pair ->
                styleDegreeVisibleLiveData.postValue(true)

                val list = pair.filter { it.first != IGNORE_VALUE_DEFAULT }
                    .map { SpinnerItem(it.second, it.first) }.toMutableList()
                list.add(0, DEFAULT_SPINNER_ITEM)
                items = list

                // 设置选中位置
                mTts.expressAs?.let { expressAs ->
                    val pos = items.indexOfFirst { it.value == expressAs.style }
                    position = max(pos, 0)
                }
                return
            }
        }
        // 列表为空
        mTts.expressAs?.style = null
        mTts.expressAs?.styleDegree = 1F
        styleDegreeVisibleLiveData.postValue(false)
    }

    // 更新角色列表
    private fun updateRoles(currentVoice: GeneralVoiceData) {
        ui.roles.apply {
            reset()
            currentVoice.localRoleList?.let { pair ->
                val list = pair.filter { it.first != IGNORE_VALUE_DEFAULT }
                    .map { SpinnerItem(it.second, it.first) }.toMutableList()
                list.add(0, DEFAULT_SPINNER_ITEM)
                items = list

                // 设置选中位置
                mTts.expressAs?.let { exp ->
                    val pos = items.indexOfFirst { it.value == exp.role }
                    position = max(pos, 0)
                }
                return@apply
            }

            // 列表为空
            mTts.expressAs?.role = null
        }
    }

    // 更新二级语言
    private fun updateSecondaryLocales(currentVoice: GeneralVoiceData) {
        ui.secondaryLocales.apply {
            reset()
            currentVoice.localSecondaryLocaleList?.let { list ->
                if (list.isNotEmpty()) {
                    items = list.map { SpinnerItem(it.second, it.first) }
                    position = items.indexOfFirst { it.value == (mTts.secondaryLocale ?: "zh-CN") }
                    return
                }
            }
            mTts.secondaryLocale = null
        }
    }

    fun doTest(
        text: String,
        onSuccess: suspend (audio: ByteArray, sampleRate: Int, mime: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (mTts.format.startsWith("raw")) {
            onFailure.invoke(Exception("raw格式无法播放，请手动换为其他格式。\nPS：不推荐使用raw-pcm，其极耗流量、卡顿不稳定，且易被微软限制IP！"))
            return
        }

        viewModelScope.runOnIO {
            val audio = try {
                mTts.onLoad()
                mTts.getAudioWithSystemParams(text, sysRate = 0)
            } catch (e: Exception) {
                withMain { onFailure.invoke(e) }
                return@runOnIO
            }

            audio?.let {
                val formats = AudioDecoder.getSampleRateAndMime(audio)
                withMain { onSuccess.invoke(it, formats.first, formats.second) }
                return@runOnIO
            }
            withMain { onFailure.invoke(Exception("音频为空")) }
        }
    }

    data class UiData(
        val apis: SpinnerData = SpinnerData(),
        val locales: SpinnerData = SpinnerData(),
        var secondaryLocales: SpinnerData = SpinnerData(),
        val voices: SpinnerData = SpinnerData(),
        val styles: SpinnerData = SpinnerData(),
        val roles: SpinnerData = SpinnerData(),
//    val formats: SpinnerData = SpinnerData()
    )
}