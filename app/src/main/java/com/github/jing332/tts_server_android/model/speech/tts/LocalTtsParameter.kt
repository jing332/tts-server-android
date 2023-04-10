package com.github.jing332.tts_server_android.model.speech.tts

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LocalTtsParameter(var type: String, var key: String, var value: String) : Parcelable {
    companion object {
        val typeList = listOf(
            "Boolean",
            "Int",
            "Float",
            "String"
        )
    }

    fun putValueFromBundle(b: Bundle) {
        when (type) {
            "Boolean" -> b.putBoolean(key, value.toBoolean())
            "Int" -> b.putInt(key, value.toInt())
            "Float" -> b.putFloat(key, value.toFloat())
            else -> b.putString(key, value)
        }
    }
}