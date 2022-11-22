package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import java.io.Serializable

@kotlinx.serialization.Serializable
data class SysTtsConfigItem(
    var uiData: SysTtsUiData, /* UI显示数据 */
    var isEnabled: Boolean = false,  /* 是否启用 */
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.DEFAULT,
    var voiceProperty: VoiceProperty, /* 朗读属性 */
) : Serializable {
    constructor() : this(
        SysTtsUiData(), false,
        ReadAloudTarget.DEFAULT,
        VoiceProperty()
    )

    constructor(isEnabled: Boolean, @ReadAloudTarget readAloudTarget: Int) : this(
        SysTtsUiData(), isEnabled,
        readAloudTarget,
        VoiceProperty()
    )
}