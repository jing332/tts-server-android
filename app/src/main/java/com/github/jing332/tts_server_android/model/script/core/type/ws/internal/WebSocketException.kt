package com.github.jing332.tts_server_android.model.script.core.type.ws.internal

import okhttp3.Response

data class WebSocketException(val response: Response? = null) : Exception()