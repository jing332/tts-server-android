package com.github.jing332.tts_server_android.model.script.core.ext

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

    @Suppress("UNCHECKED_CAST")
    private fun postMultipart(type: String, form: Map<String, Any>): MultipartBody.Builder {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(type.toMediaType())

        form.forEach { entry ->
            when (entry.value) {
                // 文件表单
                is Map<*, *> -> {
                    val filePartMap = entry.value as Map<String, Any>
                    val fileName = filePartMap["fileName"] as? String
                    val body = filePartMap["body"]
                    val contentType = filePartMap["contentType"] as? String

                    val mediaType = contentType?.toMediaType()
                    val requestBody = when (body) {
                        is File -> body.asRequestBody(mediaType)
                        is ByteArray -> body.toRequestBody(mediaType)
                        is String -> body.toRequestBody(mediaType)
                        else -> body.toString().toRequestBody()
                    }

                    multipartBody.addFormDataPart(entry.key, fileName, requestBody)
                }

                // 常规表单
                else -> multipartBody.addFormDataPart(entry.key, entry.value as String)
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