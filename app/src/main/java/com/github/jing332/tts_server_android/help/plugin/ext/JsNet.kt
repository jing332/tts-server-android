package com.github.jing332.tts_server_android.help.plugin.ext

import androidx.annotation.Keep
import com.drake.net.Net
import com.drake.net.exception.ConvertException
import okhttp3.Headers.Companion.toHeaders
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

@Keep
open class JsNet {
    @JvmOverloads
    fun httpGet(url: String, headers: Map<String, String>? = null): Response {
        return Net.get(url) {
            headers?.let { setHeaders(it.toHeaders()) }
        }.execute()
    }

    /**
     * HTTP GET
     */
    @JvmOverloads
    fun httpGetString(url: String, headers: Map<String, String>? = null): String? {
        kotlin.runCatching {
            return Net.get(url) {
                headers?.let { setHeaders(it.toHeaders()) }
            }.execute<String>()
        }.onFailure {
            it.printStackTrace()
            throw it
        }

        return null
    }

    @JvmOverloads
    fun httpGetBytes(url: String, headers: Map<String, String>? = null): ByteArray? {
        return try {
            Net.get(url) {
                headers?.let { setHeaders(it.toHeaders()) }
            }.execute<ByteArray>()
        } catch (e: ConvertException) {
            throw Exception("返回值非Bytes, HTTP-${e.response.code}=${e.response.message}")
        }
    }

    /**
     * HTTP POST
     */
    @JvmOverloads
    fun httpPost(url: String, body: String, headers: Map<String, String>? = null): Response {
        return Net.post(url) {
            this.body = body.toRequestBody()
            headers?.let {
                setHeaders(it.toHeaders())
            }
        }.execute()
    }
}