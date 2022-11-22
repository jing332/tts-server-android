package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import java.io.Serializable

/* 旧配置 已弃用*/
@kotlinx.serialization.Serializable
data class CompatSysTtsConfigItem(
    var uiData: UiData, /* UI显示数据 */
    var isEnabled: Boolean = false,  /* 是否启用 */
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.DEFAULT,
    var voiceProperty: MsTtsProperty, /* 朗读属性 */
) : Serializable {
    @kotlinx.serialization.Serializable
    data class UiData(
        var displayName: String,
    ) : Serializable
}