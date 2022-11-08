package com.github.jing332.tts_server_android.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.service.systts.help.TtsFormatManger

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    val ttsCfg: MutableLiveData<SysTtsConfig> by lazy { MutableLiveData() }
    val replacedItemDataLiveData: MutableLiveData<ReplacedData> by lazy { MutableLiveData() }
    val appendItemDataLiveData: MutableLiveData<SysTtsConfigItem> by lazy { MutableLiveData() }

    fun loadData() {
        ttsCfg.value = SysTtsConfig.read()
    }

    fun saveData() {
        ttsCfg.value?.save()
    }

    fun onCheckBoxChanged(position: Int, checked: Boolean): Boolean {
        ttsCfg.value?.apply {
            /* 检测多语音是否开启 */
            if (checked && ttsCfg.value?.isMultiVoice == false) {
                val target = list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.DEFAULT
                if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                    return false
                }
            }

            list[position].let { data ->
                if (checked) { //确保同类型只可单选
                    ttsCfg.value!!.list.forEachIndexed { index, it ->
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
        ttsCfg.value?.apply {
            val aside = currentAsideItem()
            val dialogue = currentDialogueItem()
            if (aside == null || dialogue == null) {
                return@apply
            } else if (aside.isEnabled && dialogue.isEnabled) {
                return TtsFormatManger.isFormatSampleEqual(aside.format, dialogue.format)
            }
        }
        return true
    }

    /* 朗读目标更改 */
    fun onReadAloudTargetChanged(position: Int, @ReadAloudTarget raTarget: Int) {
        val data = ttsCfg.value!!.list[position]
        data.readAloudTarget = raTarget
        replacedItemDataLiveData.value = ReplacedData(data, position, true)
    }

    data class ReplacedData(
        var sysTtsConfigItem: SysTtsConfigItem,
        var position: Int,
        val updateUi: Boolean,
    )

}