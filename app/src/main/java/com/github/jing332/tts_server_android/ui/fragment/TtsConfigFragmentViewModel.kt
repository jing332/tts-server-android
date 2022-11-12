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

    val ttsCfgLiveData: MutableLiveData<SysTtsConfig> by lazy { MutableLiveData() }
    val replacedItemDataLiveData: MutableLiveData<ReplacedData> by lazy { MutableLiveData() }
    val appendItemDataLiveData: MutableLiveData<SysTtsConfigItem> by lazy { MutableLiveData() }

    fun loadData() {
        viewModelScope.runOnIO { ttsCfgLiveData.postValue(SysTtsConfig.read()) }
    }

    fun saveData() {
        ttsCfgLiveData.value?.save()
    }

    fun onMenuIsSplitChanged(checked: Boolean) {
        ttsCfgLiveData.value?.isSplitSentences = checked
        ttsCfgLiveData.value?.save()
    }

    fun onMenuMultiVoiceChanged(checked: Boolean) {
        ttsCfgLiveData.value?.isMultiVoice = checked
        ttsCfgLiveData.value?.save()
    }

    fun onCheckBoxChanged(position: Int, checked: Boolean): Boolean {
        ttsCfgLiveData.value?.apply {
            /* 检测多语音是否开启 */
            if (checked && ttsCfgLiveData.value?.isMultiVoice == false) {
                val target = list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.DEFAULT
                if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                    return false
                }
            }

            list[position].let { data ->
                if (checked) { //确保同类型只可单选
                    ttsCfgLiveData.value!!.list.forEachIndexed { index, it ->
                        if (it.readAloudTarget == data.readAloudTarget) {
                            it.isEnabled = false
                            replacedItemDataLiveData.value = ReplacedData(it, index, true)
                        }
                    }
                }

                /* 更新position */
                data.isEnabled = checked
                replacedItemDataLiveData.value = ReplacedData(data, position, false)
            }
        }
        return true
    }

    /* 添加或编辑Item */
    fun onEditActivityResult(data: SysTtsConfigItem, position: Int) {
        if (position >= 0) { //编辑
            replacedItemDataLiveData.value = ReplacedData(data, position, true)
        } else { //添加
            appendItemDataLiveData.value = data
        }
    }

    /**
     * 检查多语音音频格式是否一致
     * @return 是否相等
     */
    fun checkMultiVoiceFormat(): Boolean {
        ttsCfgLiveData.value?.apply {
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
    fun onReadAloudTargetChanged(position: Int, @ReadAloudTarget raTarget: Int) {
        val data = ttsCfgLiveData.value!!.list[position]
        data.readAloudTarget = raTarget
        replacedItemDataLiveData.value = ReplacedData(data, position, true)
    }

    /* 按API排序 */
    fun sortListByApi() {
        ttsCfgLiveData.value?.apply {
            list.sortBy { it.voiceProperty.api }
            ttsCfgLiveData.value = this
        }
    }

    /* 按朗读目标排序 */
    fun sortListByRaTarget() {
        ttsCfgLiveData.value?.apply {
            list.sortBy { it.readAloudTarget }
            ttsCfgLiveData.value = this
        }
    }

    /* 按显示名称排序 */
    fun sortListByDisplayName() {
        ttsCfgLiveData.value?.apply {
            list.sortBy { it.uiData.displayName }
            ttsCfgLiveData.value = this
        }
    }


    fun uploadConfigToUrl(json: String): Result<String> {
        return try {
            val url = Tts_server_lib.uploadConfig(json)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(ttsCfgLiveData.value?.list)
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }

    fun importConfigByUrl(url: String): String? {
        val jsonStr = try {
            Tts_server_lib.httpGet(url, "")
        } catch (e: Exception) {
            return e.message
        }
        return importConfig(jsonStr.decodeToString())
    }

    fun importConfig(json: String): String? {
        try {
            val list = App.jsonBuilder.decodeFromString<List<SysTtsConfigItem>>(json)
            list.forEach {
                it.isEnabled = false
                appendItemDataLiveData.value = it
            }

        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    data class ReplacedData(
        var sysTtsConfigItem: SysTtsConfigItem,
        var position: Int,
        val updateUi: Boolean,
    )

}