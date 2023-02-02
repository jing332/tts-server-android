package com.github.jing332.tts_server_android.service.forwarder.system

import kotlinx.serialization.Serializable

@Serializable
data class EngineInfo(val name: String, val label: String)