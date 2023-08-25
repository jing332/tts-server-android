package com.github.jing332.tts_server_android.data.entities.systts

import android.os.Parcelable
import androidx.room.*
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
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
    @ColumnInfo(defaultValue = AbstractListGroup.DEFAULT_GROUP_ID.toString())
    var groupId: Long = AbstractListGroup.DEFAULT_GROUP_ID,

    var displayName: String? = null,

    var isEnabled: Boolean = false,

    @Embedded("speechRule_")
    var speechRule: SpeechRuleInfo = SpeechRuleInfo(),

    var tts: ITextToSpeechEngine,

    // 索引 排序用
    @ColumnInfo(defaultValue = "0")
    var order: Int = 0,
) : Parcelable {
    // 朗读目标
    @Deprecated("")
    @SpeechTarget
    @SerialName("readAloudTarget")
    @get:Ignore
    var speechTarget: Int
        get() = speechRule.target
        set(value) {
            speechRule.target = value
        }

    @Deprecated("")
    @SpeechTarget
    @SerialName("isStandby")
    @get:Ignore
    var isStandby: Boolean
        get() = speechRule.isStandby
        set(value) {
            speechRule.isStandby = value
        }

    // 转换器
    class Converters {
        companion object {
            @OptIn(ExperimentalSerializationApi::class)
            val json by lazy {
                Json {
                    ignoreUnknownKeys = true //忽略未知
                    explicitNulls = false //忽略为null的字段
                    allowStructuredMapKeys = true
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
        fun ttsToString(tts: ITextToSpeechEngine): String {
            return json.encodeToString(tts)
        }

        @TypeConverter
        fun stringToTts(json: String?): ITextToSpeechEngine {
            return decodeFromString<ITextToSpeechEngine>(json).run { this ?: MsTTS() }
        }
    }
}