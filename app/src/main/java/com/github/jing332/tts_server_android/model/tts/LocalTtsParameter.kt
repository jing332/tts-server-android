package com.github.jing332.tts_server_android.model.tts

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
            "Boolean" -> {
                val bool = when (value) {
                    "true", "1" -> true
                    "false", "0" -> false
                    else -> return
                }

                b.putBoolean(key, bool)
            }
            "Int" -> b.putInt(key, value.toInt())
            "Float" -> b.putFloat(key, value.toFloat())
            else -> b.putString(key, value)
        }
    }
}