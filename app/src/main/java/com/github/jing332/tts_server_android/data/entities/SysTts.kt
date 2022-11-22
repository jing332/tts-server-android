package com.github.jing332.tts_server_android.data.entities

import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.MsTtsProperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@kotlinx.serialization.Serializable
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
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.DEFAULT,

    // 内置的微软TTS
    var msTts: MsTtsProperty? = null,

    // 自定义的HttpTTS
    var httpTts: HttpTtsProperty? = null
) : java.io.Serializable {
    val isMsTts: Boolean
        inline get() {
            return msTts != null
        }

    val isHttpTts: Boolean
        inline get() {
            return httpTts != null
        }

    val readAloudTargetString: String
        inline get() {
            return ReadAloudTarget.toString(readAloudTarget)
        }

    /**
     * 内容描述
     */
    val description: String?
        inline get() {
            return if (isMsTts) msTts?.description else null
        }


    class Converters {
        companion object {
            @OptIn(ExperimentalSerializationApi::class)
            val json by lazy {
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false //忽略为null的字段} }
                }
            }

            private inline fun <reified T> decodeFromString(s: String?): T? {
                if (s == null) return null
                return try {
                    json.decodeFromString<T>(s.toString())
                } catch (e: Exception) {
                    null
                }
            }
        }

        @TypeConverter
        fun msTtsPropertyToString(pro: MsTtsProperty?): String =
            json.encodeToString(pro)

        @TypeConverter
        fun stringTomsTtsProperty(json: String?) = decodeFromString<MsTtsProperty>(json)

        @TypeConverter
        fun httpTtsToString(httpTts: HttpTtsProperty?) = json.encodeToString(httpTts)

        @TypeConverter
        fun stringToHttpTts(json: String?) = decodeFromString<HttpTtsProperty>(json)
    }
}
