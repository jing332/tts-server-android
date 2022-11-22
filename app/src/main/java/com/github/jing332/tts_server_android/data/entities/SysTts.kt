package com.github.jing332.tts_server_android.data.entities

import android.os.Parcelable
import androidx.room.*
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsUiData
import com.github.jing332.tts_server_android.data.VoiceProperty
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


@kotlinx.serialization.Serializable
@TypeConverters(SysTts.Converters::class)
@Entity(tableName = "sysTts")
data class SysTts(
    @kotlinx.serialization.Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 是否启用
    @kotlinx.serialization.Transient
    @ColumnInfo(name = "is_enabled")
    var isEnabled: Boolean = false,

    // UI显示名称
    @ColumnInfo(name = "ui_data")
    var uiData: SysTtsUiData = SysTtsUiData(""),


    // 朗读目标
    @ColumnInfo(name = "read_aloud_target")
    @ReadAloudTarget var readAloudTarget: Int = ReadAloudTarget.DEFAULT,

    // 微软TTS属性
    @ColumnInfo(name = "ms_tts_property")
    var msTtsProperty: VoiceProperty? = null,

    // 自定义的HttpTTS
    @ColumnInfo(name = "http_tts")
    var httpTts: HttpTts? = null
) : java.io.Serializable {
    val isMsTts: Boolean
        get() {
            return msTtsProperty != null
        }

    val isHttpTts: Boolean
        get() {
            return httpTts != null
        }

    @Parcelize
    @kotlinx.serialization.Serializable
    data class HttpTts(var name: String) : java.io.Serializable, Parcelable

    class Converters {
        @TypeConverter
        fun uiDataToString(uiData: SysTtsUiData?): String = App.jsonBuilder.encodeToString(uiData)

        @TypeConverter
        fun stringToUiData(json: String?) = decodeFromString<SysTtsUiData>(json)

        @TypeConverter
        fun msTtsPropertyToString(pro: VoiceProperty?): String = App.jsonBuilder.encodeToString(pro)

        @TypeConverter
        fun stringTomsTtsProperty(json: String?) = decodeFromString<VoiceProperty>(json)

        @TypeConverter
        fun httpTtsToString(httpTts: HttpTts?) = App.jsonBuilder.encodeToString(httpTts)

        @TypeConverter
        fun stringToHttpTts(json: String?) = decodeFromString<HttpTts>(json)

        private inline fun <reified T> decodeFromString(json: String?): T? {
            if (json == null) return null
            return try {
                App.jsonBuilder.decodeFromString<T>(json.toString())
            } catch (e: Exception) {
                null
            }
        }
    }


}

