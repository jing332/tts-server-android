package com.github.jing332.tts_server_android.ui.systts.list

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.SysTtsConfig

class SysTtsListGroupViewModel : ViewModel() {
    fun onCheckBoxChanged(list: List<SystemTts>, position: Int, checked: Boolean): Boolean {
        //检测多语音是否开启
        if (checked && !SysTtsConfig.isMultiVoiceEnabled) {
            val target =
                list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.ALL
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        list[position].let { data ->
            if (checked && !SysTtsConfig.isVoiceMultipleEnabled) { // 多选关闭下 确保同类型只可单选
                list.forEach {
                    if (it.readAloudTarget == data.readAloudTarget) {
                        appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                    }
                }
            }

            appDb.systemTtsDao.updateTts(data.copy(isEnabled = checked))
        }
        return true
    }
}