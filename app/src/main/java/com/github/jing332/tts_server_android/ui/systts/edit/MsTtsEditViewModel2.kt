package com.github.jing332.tts_server_android.ui.systts.edit

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.model.tts.ExpressAs
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.runOnIO
import kotlinx.coroutines.launch
import kotlin.math.max

class MsTtsEditViewModel2 : ViewModel() {
    companion object {
        const val TAG = "MsTtsEditViewModel2"
        private val repo: MsTtsEditRepository by lazy { MsTtsEditRepository() }

        // 男
        private const val MALE_IMAGE_RES_ID = R.drawable.ic_baseline_male_24

        // 女
        private const val FEMALE_IMAGE_RES_ID = R.drawable.ic_baseline_female_24

        // 风格和角色中忽略此字符串
        private const val IGNORE_VALUE_DEFAULT = "Default"

        // 默认的SpinnerItem 用于风格和角色
        private val DEFAULT_SPINNER_ITEM: SpinnerItem by lazy { SpinnerItem("默认", "") }
    }

    lateinit var mTts: MsTTS
    lateinit var mData: SysTts

    // 全部的list
    private lateinit var mAllVoiceList: List<GeneralVoiceData>

    // 当前地区下的list
    private lateinit var mCurrentVoiceList: List<GeneralVoiceData>

    // 当前的发音人
    private var mCurrentVoice: GeneralVoiceData? = null

    // UI数据
    var ui: UiData = UiData(
        ""
    )

    fun getData(displayName: String): SysTts {
        if (displayName.isEmpty() || isAutoGenDisplayName(displayName)) { // 为自动生成的
            mData.displayName = getDisplayName()
        } else { // 为用户自定义
            mData.displayName = displayName
        }
        if (mTts.api == MsTtsApiType.EDGE) {
            mTts.expressAs = null
        }
        return mData
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

    fun initUserData(data: SysTts) {
        mData = data
        mTts = data.tts as MsTTS

        mRaTargetLiveData.value = mData.readAloudTarget

        mData.displayName?.let {
            ui.displayName = if (isAutoGenDisplayName(it)) "" else it
        }

        ui.apis.position = mTts.api
    }


    // 朗读目标的位置
    private val mRaTargetLiveData: MutableLiveData<Int> by lazy { MutableLiveData() }
    val raTargetLiveData: LiveData<Int> by lazy { mRaTargetLiveData }

    fun raTargetChanged(position: Int) {
        if (mRaTargetLiveData.value != position) mRaTargetLiveData.value = position
        mData.readAloudTarget = position
    }

    interface CallBack {
        fun onStart(@MsTtsApiType api: Int)
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
        viewModelScope.launch {
            val spinner = ui.apis
            mTts.api = spinner.position
            mCallback?.onStart(mTts.api)

            kotlin.runCatching {
                // 开始获取 当前接口下的所有数据
                mAllVoiceList = repo.voicesByApi(spinner.position)
            }.onFailure {
                it.printStackTrace()
                mCallback?.onDone(Result.failure(it))
                return@launch
            }

            // 更新地区列表
            updateLocales(mAllVoiceList)

            mCallback?.onDone(Result.success(Unit))
        }
    }

    /**
     * 初始化Spinner
     */
    fun init(list: List<Pair<String, Int>>) {
        // 接口
        ui.apis.items = list.map {
            SpinnerItem(
                displayText = it.first, value = it.first,
                imageResId = it.second
            )
        }

        ui.apis.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) reloadApiData()
        }

        // 地区
        ui.locales.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                mTts.locale = (ui.locales.selectedItem?.value ?: "") as String
                viewModelScope.runOnIO { updateVoices(mAllVoiceList) }
            }
        }

        // 语音
        ui.voices.addOnPropertyChangedCallback { _, propertyId ->
            if (propertyId == BR.position) {
                viewModelScope.runOnIO {
                    mCurrentVoice = ui.voices.selectedItem?.value as GeneralVoiceData
                    mCurrentVoice?.let {
                        mTts.voiceName = it.voiceName
                        mTts.voiceId = it.voiceId
                        updateStyles(it)
                        updateRoles(it)
                        updateSecondaryLocales(it)
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
            items =
                allList.map { SpinnerItem(it.localeName, it.locale) }.distinctBy { it.value }
            val pos = items.indexOfFirst { it.value == mTts.locale }
            position = max(pos, 0)
        }
    }


    // 更新发音列表
    private fun updateVoices(data: List<GeneralVoiceData>) {
        ui.voices.apply {
            // 筛选出当前地区的Voice
            mCurrentVoiceList = data.filter { it.locale == ui.locales.selectedItem?.value }

            items = mCurrentVoiceList.map {
                val imgResId = if (it.isMale) MALE_IMAGE_RES_ID else FEMALE_IMAGE_RES_ID
                SpinnerItem(
                    displayText = "${it.localVoiceName} (${it.voiceName})", value = it,
                    imageResId = imgResId
                )
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
            }
        }
    }

    // 更新二级语言
    private fun updateSecondaryLocales(currentVoice: GeneralVoiceData) {
        ui.secondaryLocales.apply {
            reset()
            currentVoice.localSecondaryLocaleList?.let { list ->
                if (list.isNotEmpty()) {
                    items = list.map { SpinnerItem(it.second, it.first) }
                    val pos = items.indexOfFirst { it.value == mTts.secondaryLocale }
                    position = max(pos, 0)
                }
            }
        }

    }
}

data class UiData(
    var displayName: String = "",
    val raTarget: SpinnerData = SpinnerData(),
    val apis: SpinnerData = SpinnerData(),
    val locales: SpinnerData = SpinnerData(),
    var secondaryLocales: SpinnerData = SpinnerData(),
    val voices: SpinnerData = SpinnerData(),
    val styles: SpinnerData = SpinnerData(),
    val roles: SpinnerData = SpinnerData(),
//    val formats: SpinnerData = SpinnerData()
)


class SpinnerData() : BaseObservable() {

    // 包装成lambda
    fun addOnPropertyChangedCallback(callback: (sender: Observable?, propertyId: Int) -> Unit) {
        super.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                callback(sender, propertyId)
            }
        })
    }

    fun reset() {
        items = listOf()
        position = 0
    }

    // View -> ViewModel 单向绑定
    @Bindable("items")
    var items: List<SpinnerItem> = emptyList()
        set(value) {
            field = value
            notifyPropertyChanged(BR.items)
        }

    // 双向绑定
    @Bindable("position")
    var position: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.position)
        }


    operator fun get(i: Int): SpinnerItem {
        return items[i]
    }

    val selectedItem: SpinnerItem?
        get() {
            return items.getOrNull(position)
        }
}