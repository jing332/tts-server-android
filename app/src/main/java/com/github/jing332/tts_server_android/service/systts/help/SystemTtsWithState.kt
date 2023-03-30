package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

data class SystemTtsWithState(
    val data: SystemTts,
    var isRateFollowSystem: Boolean = false,
    var isPitchFollowSystem: Boolean = false
)