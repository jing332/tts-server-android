package com.github.jing332.tts_server_android.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsSettingsViewModel"
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

    fun onCheckBoxChanged(position: Int, checked: Boolean) {
        ttsCfg.value?.list?.get(position)?.let { item ->
            if (checked) {
                ttsCfg.value!!.list.forEachIndexed { index, it ->
                    if (it.readAloudTarget == item.readAloudTarget) {
                        it.isEnabled = false
                        replacedItemDataLiveData.value = ReplacedData(it, index, true)
                    }
                }
            }

            when (item.readAloudTarget) {
                ReadAloudTarget.ASIDE -> ttsCfg.value?.currentAside = position
                ReadAloudTarget.DIALOGUE -> ttsCfg.value?.currentDialogue = position
                else -> ttsCfg.value?.currentSelected = position
            }
            item.isEnabled = checked
            replacedItemDataLiveData.value = ReplacedData(item, position, false)
        }
    }

    fun onEditActivityResult(data: SysTtsConfigItem, position: Int) {
        if (position >= 0) { // 为编辑数据
            replacedItemDataLiveData.value = ReplacedData(data, position, true)
            saveData()
        } else {
            appendItemDataLiveData.value = data
        }
    }

    data class ReplacedData(
        var sysTtsConfigItem: SysTtsConfigItem,
        var position: Int,
        val updateUi: Boolean,
    )

}