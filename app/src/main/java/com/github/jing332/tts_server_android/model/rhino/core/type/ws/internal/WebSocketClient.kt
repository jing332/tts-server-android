package com.github.jing332.tts_server_android.model.rhino.core.type.ws.internal

import android.util.Log
import com.drake.net.utils.withIO
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketClient : WebSocketListener() {
    private lateinit var webSocket: WebSocket

    companion object {
        private val client = OkHttpClient.Builder().writeTimeout(1, TimeUnit.SECONDS).build()
        private const val TAG = "WebSocketClient"
    }

    var event: IWebSocketEvent? = null

    var connectStatus: Status = Status.Closed
        private set

    fun connect(req: Request) {
        webSocket = client.newWebSocket(req, this@WebSocketClient)
        connectStatus = Status.Connecting
    }

    suspend fun connectSync(req: Request): Boolean = withIO {
        connect(req)
        while (isActive) {
            delay(100)
            when (connectStatus) {
                Status.Opened -> return@withIO true
                is Status.Failure ->
                    (connectStatus as Status.Failure).apply {
                        throw WebSocketException(response).initCause(t)
                    }

                else -> {}
            }
        }
        if (connectStatus != Status.Connecting) return@withIO true

        return@withIO false
    }

    /**
     * 同步连接WS
     * @return 是否成功
     */
    suspend fun connectSync(url: String): Boolean = connectSync(Request.Builder().url(url).build())

    /**
     * 重连WS
     * @return 是否成功
     */
    suspend fun reConnect() = connectSync(webSocket.request())

    fun send(text: String): Boolean {
        Log.d(TAG, "send: $text")
        return webSocket.send(text)
    }

    fun send(bytes: ByteString): Boolean {
        Log.d(TAG, "send: $bytes")
        return webSocket.send(bytes)
    }

    fun cancel() = webSocket.cancel()
    fun close() = webSocket.close(1000, null)

    fun close(code: Int, reason: String? = null) = webSocket.close(code, reason)

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "onClosed: $code, $reason")
        super.onClosed(webSocket, code, reason)
        connectStatus = Status.Closed
        event?.onClosed(code, reason)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "onClosing: $code, $reason")
        super.onClosing(webSocket, code, reason)
        connectStatus = Status.Closing
        event?.onClosing(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure: $t, $response ${response?.body?.string()}")
        super.onFailure(webSocket, t, response)
        connectStatus = Status.Failure(t, response)
        event?.onFailure(t)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        connectStatus = Status.Opened
        event?.onOpen(response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
        super.onMessage(webSocket, text)

        event?.onMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "onMessage: $bytes")
        super.onMessage(webSocket, bytes)

        event?.onMessage(bytes)
    }

    sealed class Status {
        object Connecting : Status()
        object Opened : Status()
        object Closed : Status()
        object Closing : Status()
        data class Failure(val t: Throwable, val response: Response?) : Status()
    }
}