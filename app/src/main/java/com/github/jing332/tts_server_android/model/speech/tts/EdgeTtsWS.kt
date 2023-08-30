package com.github.jing332.tts_server_android.model.speech.tts

import android.util.Log
import cn.hutool.core.lang.UUID
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.model.rhino.core.type.ws.internal.WebSocketException
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class EdgeTtsWS : WebSocketListener() {
    companion object {
        const val TAG = "EdgeTtsWS"

        private const val wssUrl =
            "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4&ConnectionId="

        private val simpleDateFormat by lazy {
            SimpleDateFormat(
                "EEE MMM dd yyyy HH:mm:ss",
                Locale.getDefault()
            )
        }
    }

    private lateinit var ws: WebSocket
    private var uuid: String = ""
    private var waitJob: Job? = null

    var connectStatus: Status = Status.Closed
        private set

    private fun connect(req: Request) {
        connectStatus = Status.Connecting
        val client = OkHttpClient.Builder().writeTimeout(5, TimeUnit.SECONDS).build()
        ws = client.newWebSocket(req, this)
    }

    private suspend fun connectSync(): Boolean = withIO {
        val req = Request.Builder().url(wssUrl + uuid).apply {
            header("Accept-Encoding", "gzip, deflate, br")
            header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
            header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.66 Safari/537.36 Edg/103.0.1264.44"
            )
        }.build()

        connect(req)
        while (isActive) {
            delay(50)
            when (connectStatus) {
                Status.Opened -> return@withIO true
                is Status.Failure ->
                    (connectStatus as Status.Failure).apply {
                        throw WebSocketException(response).initCause(t)
                    }

                else -> {}
            }
        }

        return@withIO connectStatus != Status.Connecting
    }

    private var outputStream: PipedOutputStream? = null
    suspend fun getAudio(
        text: String,
        voice: String,
        rate: Int,
        volume: Int,
        pitch: Int,
        format: String
    ): InputStream = getAudio(generateSSML(text, voice, rate, volume, pitch), format)

    suspend fun getAudio(ssml: String, format: String): InputStream = coroutineScope {
        uuid = UUID.randomUUID().toString(true)
        outputStream = PipedOutputStream()

        if (connectStatus != Status.Opened) connectSync()

        sendConfig(format)
        sendSSML(ssml)

        waitJob = launch { awaitCancellation() }.job
        waitJob?.join() // 等待响应: Path:turn.start

        if (outputStream == null) {
            when (connectStatus) {
                is Status.Failure ->
                    (connectStatus as Status.Failure).apply {
                        throw WebSocketException(response).initCause(t)
                    }

                is Status.Closing ->
                    (connectStatus as Status.Closing).apply {
                        throw Exception("WebSocket is closing: $code $reason")
                    }

                else -> {
                    throw Exception("outputStream is null")
                }
            }
        }

        return@coroutineScope PipedInputStream(outputStream)
    }

    /**
     * 取消并关闭 Websocket 连接
     */
    fun cancelConnect() {
        ws.cancel()
        connectStatus = Status.Closed
    }

    private val currentISOTime: String
        get() = simpleDateFormat.format(System.currentTimeMillis())

    private fun sendSSML(ssml: String) {
        Log.d(TAG, "sendSSML: $ssml")
        val msg =
            "Path: ssml\r\nX-RequestId: $uuid\r\nX-Timestamp: $currentISOTime\r\nContent-Type: application/ssml+xml\r\n\r\n$ssml"
        ws.send(msg)
    }

    private fun sendConfig(format: String) {
        Log.d(TAG, "sendConfig: $format")
        val msg =
            "X-Timestamp:$currentISOTime\r\nContent-Type:application/json; charset=utf-8\r\nPath:speech.config\r\n\r\n" +
                    """{"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"$format"}}}}""".trimIndent()

        ws.send(msg)
    }

    private fun generateSSML(
        text: String,
        voice: String,
        rate: Int,
        volume: Int,
        pitch: Int
    ): String {
        return """
            <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="en-US">
                <voice name="$voice">
                    <prosody rate="${rate}%" volume="${volume}%" pitch="${pitch}%">
                        ${xmlEscape(text)}
                    </prosody>
                </voice>
            </speak>
        """.trimIndent()
    }

    private fun xmlEscape(s: String): String {
        return s.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("&", "&amp;").replace("/", "").replace("\\", "")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connectStatus = Status.Opened
        Log.d(TAG, "onOpen: $response")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d(TAG, "onClosing: $code $reason")

        connectStatus = Status.Closing(code, reason)
        outputStream?.close()
        outputStream = null
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        connectStatus = Status.Closed
        Log.d(TAG, "onClosed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.w(TAG, "onFailure: $response", t)
        connectStatus = Status.Failure(t, response)
        outputStream?.close()
        outputStream = null
        waitJob?.cancel()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "onMessage: $bytes")

        val index = bytes.indexOf("Path:audio".toByteArray())
        val data = bytes.substring(index + 12);

        outputStream?.write(data.toByteArray())
        outputStream?.flush()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")

        if (text.contains("Path:turn.end")) {
            Log.d(TAG, "turn.end")
            outputStream?.close()
            outputStream = null
        } else if (text.contains("Path:turn.start")) {
            waitJob?.cancel()
            waitJob = null
        }

    }

    sealed class Status {
        data object Connecting : Status()
        data object Opened : Status()
        data object Closed : Status()
        data class Closing(val code: Int, val reason: String) : Status()
        data class Failure(val t: Throwable, val response: Response?) : Status()
    }

}