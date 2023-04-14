package com.github.jing332.tts_server_android.data.entities

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object TypeConverterUtils {
    @OptIn(ExperimentalSerializationApi::class)
    val json by lazy {
        Json {
            ignoreUnknownKeys = true //忽略未知
            explicitNulls = false //忽略为null的字段
            isLenient = true //忽略不符合json规范的字段
            allowStructuredMapKeys = true
        }
    }

    inline fun <reified T> decodeFromString(s: String?): T? {
        if (s == null) return null
        return try {
            json.decodeFromString<T>(s.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T> encodeToString(value :T?): String? {
        if (value == null) return null
        return try {
            json.encodeToString<T>(value)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}