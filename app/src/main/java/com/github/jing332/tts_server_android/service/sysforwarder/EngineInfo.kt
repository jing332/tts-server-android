package com.github.jing332.tts_server_android.service.sysforwarder

import kotlinx.serialization.Serializable

@Serializable
data class EngineInfo(val name: String, val label: String)