package com.github.jing332.tts_server_android.service.sysforwarder

import kotlinx.serialization.Serializable

@Serializable
data class VoiceInfo(
    val name: String,
    val locale: String,
    val localeName: String,
    val features: List<String>? = null
)