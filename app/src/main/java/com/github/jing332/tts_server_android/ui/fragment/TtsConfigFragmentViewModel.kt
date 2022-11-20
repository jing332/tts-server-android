package com.github.jing332.tts_server_android.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.service.systts.help.TtsFormatManger
import com.github.jing332.tts_server_android.util.runOnIO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    val listLiveData: MutableLiveData<ArrayList<SysTtsConfigItem>> by lazy { MutableLiveData() }
    val appendItemDataLiveData: MutableLiveData<SysTtsConfigItem> by lazy { MutableLiveData() }
    val replacedItemDataLiveData: MutableLiveData<ReplacedData> by lazy { MutableLiveData() }

    lateinit var mTtsCfg: SysTtsConfig

    val ttsConfig: SysTtsConfig?
        get() {
            return if (this::mTtsCfg.isInitialized) mTtsCfg else null
        }

    fun loadData() {
        viewModelScope.runOnIO {
            mTtsCfg = SysTtsConfig.read()
            listLiveData.postValue(mTtsCfg.list)
        }
    }

    fun saveData() {
        mTtsCfg.save()
    }

    fun removeItem(position: Int) {
        mTtsCfg.list.removeAt(position)
        mTtsCfg.save()
    }

    fun onMenuIsSplitChanged(checked: Boolean) {
        mTtsCfg.isSplitSentences = checked
        mTtsCfg.save()
    }

    fun onMenuMultiVoiceChanged(checked: Boolean) {
        mTtsCfg.isMultiVoice = checked
        mTtsCfg.save()
    }

    fun onReplaceSwitchChanged(isChecked: Boolean) {
        mTtsCfg.isReplace = isChecked
        mTtsCfg.save()
    }

    fun onCheckBoxChanged(position: Int, checked: Boolean): Boolean {
        /* 检测多语音是否开启 */
        if (checked && !mTtsCfg.isMultiVoice) {
            val target =
                mTtsCfg.list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.DEFAULT
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        mTtsCfg.list[position].let { data ->
            if (checked) { //确保同类型只可单选
                mTtsCfg.list.forEachIndexed { index, it ->
                    if (it.readAloudTarget == data.readAloudTarget) {
                        it.isEnabled = false
                        replaceItem(it, index)
                    }
                }
            }

            /* 更新position */
            data.isEnabled = checked
            replaceItem(data, position, false)
        }
        return true
    }

    /* 添加或编辑Item */
    fun onEditActivityResult(data: SysTtsConfigItem, position: Int) {
        if (position >= 0) { //编辑
            replaceItem(data, position)
        } else { //添加
            appendItem(data)
        }
    }

    /* 追加Item 同时保存配置文件 */
    private fun appendItem(data: SysTtsConfigItem) {
        mTtsCfg.list.add(data)
        appendItemDataLiveData.value = data
    }

    /* 替换Item 同时保存配置文件 */
    private fun replaceItem(data: SysTtsConfigItem, position: Int, isUpdateUi: Boolean = true) {
        mTtsCfg.list[position] = data
        mTtsCfg.save()
        replacedItemDataLiveData.value = ReplacedData(data, position, isUpdateUi)
    }

    /**
     * 检查多语音音频格式是否一致
     * @return 是否相等
     */
    fun checkMultiVoiceFormat(): Boolean {
        mTtsCfg.apply {
            val aside = currentAsideItem()
            val dialogue = currentDialogueItem()
            if (aside == null || dialogue == null) {
                return@apply
            } else if (aside.isEnabled && dialogue.isEnabled) {
                return TtsFormatManger.isFormatSampleEqual(
                    aside.voiceProperty.format,
                    dialogue.voiceProperty.format
                )
            }
        }
        return true
    }

    /* 朗读目标更改 */
    fun setReadAloudTarget(position: Int, @ReadAloudTarget raTarget: Int) {
        val data = mTtsCfg.list[position]
        data.readAloudTarget = raTarget
        replaceItem(data, position)
    }

    /* 按API排序 */
    fun sortListByApi() {
        mTtsCfg.apply {
            list.sortBy { it.voiceProperty.api }
            listLiveData.value = list
        }
    }

    /* 按朗读目标排序 */
    fun sortListByRaTarget() {
        mTtsCfg.apply {
            list.sortBy { it.readAloudTarget }
            listLiveData.value = list
        }
    }

    /* 按显示名称排序 */
    fun sortListByDisplayName() {
        mTtsCfg.apply {
            list.sortBy { it.uiData.displayName }
            listLiveData.value = list
        }
    }

    /* 上传配置到URL */
    fun uploadConfigToUrl(json: String): Result<String> {
        return try {
            val url = Tts_server_lib.uploadConfig(json)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* 导出配置 */
    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(mTtsCfg.list)
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }

    /* 从URL导入配置 */
    fun importConfigByUrl(url: String): String? {
        val jsonStr = try {
            Tts_server_lib.httpGet(url, "")
        } catch (e: Exception) {
            return e.message
        }
        return importConfig(jsonStr.decodeToString())
    }

    /* 从json导入配置 */
    fun importConfig(json: String): String? {
        try {
            val list = App.jsonBuilder.decodeFromString<List<SysTtsConfigItem>>(json)
            list.forEach {
                it.isEnabled = false
                appendItem(it)
            }
            mTtsCfg.save()

        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    /* 设置最小对话汉字数量 */
    fun setMinDialogueLength(length: Int) {
        mTtsCfg.minDialogueLength = length
        mTtsCfg.save()
    }

    /* 音频超时 */
    var audioRequestTimeout: Int
        get() {
            return mTtsCfg.timeout
        }
        set(value) {
            mTtsCfg.timeout = value
            mTtsCfg.save()
        }

    data class ReplacedData(
        var sysTtsConfigItem: SysTtsConfigItem,
        var position: Int,
        val updateUi: Boolean,
    )

}