package com.github.jing332.tts_server_android.model.rhino.core.type.ws

import com.github.jing332.tts_server_android.model.rhino.core.type.JClass
import com.github.jing332.tts_server_android.model.rhino.core.type.ws.internal.IWebSocketEvent
import com.github.jing332.tts_server_android.model.rhino.core.type.ws.internal.WebSocketClient
import com.github.jing332.tts_server_android.model.rhino.core.type.ws.internal.WebSocketClient.*
import okhttp3.Request
import okhttp3.Response
import okio.ByteString

@Suppress("unused")
class JWebSocket(
    @JvmField val url: String,
    @JvmField val headers: Map<CharSequence, CharSequence>? = null
) : JClass() {
    companion object {
        const val CONNECTING = 0
        const val OPEN = 1
        const val CLOSING = 2
        const val CLOSED = 3
        const val FAILURE = 4
    }

    fun connect() {
        val req = Request.Builder().url(url)
        headers?.forEach { req.header(it.key.toString(), it.value.toString()) }
        ws.connect(req.build())

        ws.event = object : IWebSocketEvent {
            override fun onOpen(response: Response) {
                tryBlock { onOpen?.invoke(response) }
            }

            override fun onMessage(text: String) {
                tryBlock { onTextMessage?.invoke(text) }
            }

            override fun onMessage(bytes: ByteString) {
                tryBlock { onByteMessage?.invoke(bytes) }
            }

            override fun onClosed(code: Int, reason: String) {
                tryBlock { onClosed?.invoke(code, reason) }
            }

            override fun onClosing(code: Int, reason: String) {
                tryBlock { onClosing?.invoke(code, reason) }
            }

            override fun onFailure(t: Throwable) {
                tryBlock { onFailure?.invoke(t) }
            }
        }
    }

    fun send(text: String): Boolean = ws.send(text)
    fun send(bytes: ByteString): Boolean = ws.send(bytes)

    @JvmOverloads
    fun close(code: Int, reason: String? = "") = ws.close(code, reason)

    fun cancel() = ws.cancel()

    private var ws: WebSocketClient = WebSocketClient()

    var onByteMessage: ((bytes: ByteString) -> Unit)? = null
    var onTextMessage: ((text: String) -> Unit)? = null
    var onOpen: ((Response) -> Unit)? = null
    var onClosed: ((code: Int, reason: String) -> Unit)? = null
    var onClosing: ((code: Int, reason: String) -> Unit)? = null
    var onFailure: ((t: Throwable) -> Unit)? = null

    val state: Int
        get() = when (ws.connectStatus) {
            Status.Connecting -> CONNECTING
            Status.Opened -> OPEN
            Status.Closing -> CLOSING
            Status.Closed -> CLOSED
            else -> FAILURE
        }
}