package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.TtsApiType
import java.io.Serializable

// 列表UI显示用Data类
@kotlinx.serialization.Serializable
data class TtsConfigListItemData(
    var displayName: String,
    @kotlinx.serialization.Transient var content: String? = null
) : Serializable {

    /**
     * 从VoiceProperty中生成UI内容
     */
    fun setContent(pro: VoiceProperty): String? {
        val rateStr = if (pro.prosody.isRateFollowSystem()) "跟随" else pro.prosody.rate
        val volume = pro.prosody.volume
        val style = if (pro.expressAs?.style?.isEmpty() == false) {
            CnLocalMap.getStyleAndRole(pro.expressAs?.style ?: "")
        } else "无"
        val styleDegree = pro.expressAs?.styleDegree ?: 1F
        val role = if (pro.expressAs?.role?.isEmpty() == false) {
            CnLocalMap.getStyleAndRole(pro.expressAs?.role ?: "")
        } else "无"
        val expressAs =
            if (pro.api == TtsApiType.EDGE) ""
            else "$style-$role | 强度: <b>${styleDegree}</b> | "
        content = "${expressAs}语速:<b>$rateStr</b> | 音量:<b>$volume</b>"
        return content
    }


    constructor() : this("", "")
}