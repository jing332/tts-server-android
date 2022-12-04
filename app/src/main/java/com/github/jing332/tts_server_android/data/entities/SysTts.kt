package com.github.jing332.tts_server_android.data.entities

import android.os.Parcelable
import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Parcelize
@Serializable
@TypeConverters(SysTts.Converters::class)
@Entity(tableName = "sysTts")
data class SysTts(
    @kotlinx.serialization.Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 是否启用
    @kotlinx.serialization.Transient
    var isEnabled: Boolean = false,

    // UI显示名称
    var displayName: String? = null,

    // 朗读目标
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.ALL,

    // TTS属性
    var tts: BaseTTS? = null,
    ) : Parcelable {
    val readAloudTargetString: String
        inline get() {
            return ReadAloudTarget.toString(readAloudTarget)
        }

    @Suppress("UNCHECKED_CAST")
    fun <T> ttsAs(): T {
        return tts as T
    }

    // 转换器
    class Converters {
        companion object {
            @OptIn(ExperimentalSerializationApi::class)
            val json by lazy {
                Json {
                    ignoreUnknownKeys = true //忽略未知
                    explicitNulls = false //忽略为null的字段
                }
            }

            private inline fun <reified T> decodeFromString(s: String?): T? {
                if (s == null) return null
                return try {
                    json.decodeFromString<T>(s.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        @TypeConverter
        fun ttsPropertyToString(pro: BaseTTS): String {
            return json.encodeToString(pro)
        }

        @TypeConverter
        fun stringToTtsProperty(json: String?): BaseTTS? {
            return decodeFromString(json)
        }
    }
}
