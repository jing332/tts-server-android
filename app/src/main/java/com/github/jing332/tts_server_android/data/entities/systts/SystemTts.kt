package com.github.jing332.tts_server_android.data.entities.systts

import android.os.Parcelable
import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
@kotlinx.parcelize.Parcelize
@TypeConverters(SystemTts.Converters::class)
@Entity(tableName = "sysTts")
data class SystemTts(
    @PrimaryKey(autoGenerate = true)
    val id: Long = System.currentTimeMillis(),

    // 所属组的ID
    @ColumnInfo(defaultValue = SystemTtsGroup.DEFAULT_GROUP_ID.toString())
    var groupId: Long = SystemTtsGroup.DEFAULT_GROUP_ID,
    // 名称
    var displayName: String? = null,

    // 是否启用
    @kotlinx.serialization.Transient
    var isEnabled: Boolean = false,

    //朗读目标
    @ReadAloudTarget
    var readAloudTarget: Int = ReadAloudTarget.ALL,

    var tts: BaseTTS
) : Parcelable {
    val raTargetString: String
        get() = ReadAloudTarget.toText(readAloudTarget)

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
        fun ttsToString(tts: BaseTTS): String {
            return json.encodeToString(tts)
        }

        @TypeConverter
        fun stringToTts(json: String?): BaseTTS? {
            return decodeFromString(json)
        }
    }
}