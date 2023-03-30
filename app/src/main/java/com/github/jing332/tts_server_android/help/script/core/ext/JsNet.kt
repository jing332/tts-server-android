package com.github.jing332.tts_server_android.help.script.core.ext

import androidx.annotation.Keep
import com.drake.net.Net
import com.drake.net.exception.ConvertException
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File

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
        return try {
            Net.get(url) {
                headers?.let { setHeaders(it.toHeaders()) }
            }.execute<String>()
        } catch (e: ConvertException) {
            throw Exception("Body is not a String, HTTP-${e.response.code}=${e.response.message}")
        }
    }

    @JvmOverloads
    fun httpGetBytes(url: String, headers: Map<String, String>? = null): ByteArray? {
        return try {
            Net.get(url) {
                headers?.let { setHeaders(it.toHeaders()) }
            }.execute<ByteArray>()
        } catch (e: ConvertException) {
            throw Exception("Body is not a Bytes, HTTP-${e.response.code}=${e.response.message}")
        }
    }

    /**
     * HTTP POST
     */
    @JvmOverloads
    fun httpPost(
        url: String,
        body: String? = null,
        headers: Map<String, String>? = null
    ): Response {
        return Net.post(url) {
            body?.let { this.body = it.toRequestBody() }
            headers?.let { setHeaders(it.toHeaders()) }
        }.execute()
    }

    private fun postMultipart(type: String, form: Map<String, Any>): MultipartBody.Builder {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(type.toMediaType())
        form.forEach {
            when (val value = it.value) {
                is Map<*, *> -> {
                    val fileName = value["fileName"] as String
                    var file = value["file"]
                    var partName = it.key
                    if (file is Map<*, *>) {
                        val ent = file.entries.last()
                        file = ent.value
                        partName = ent.key as String
                    }

                    val mediaType = (value["contentType"] as? String)?.toMediaType()
                    val requestBody = when (file) {
                        is File -> file.asRequestBody(mediaType)
                        is ByteArray -> file.toRequestBody(mediaType)
                        is String -> file.toRequestBody(mediaType)
                        else -> file.toString().toRequestBody()
                    }

                    multipartBody.addFormDataPart(partName, fileName, requestBody)
                }

                else -> multipartBody.addFormDataPart(it.key, it.value.toString())
            }
        }
        return multipartBody
    }

    @JvmOverloads
    fun httpPostMultipart(
        url: String,
        form: Map<String, Any>,
        type: String = "multipart/form-data",
        headers: Map<String, String>? = null
    ): Response {
        return Net.post(url) {
            headers?.let { setHeaders(it.toHeaders()) }
            body = postMultipart(type, form).build()
        }.execute()
    }
}