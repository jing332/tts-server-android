package com.github.jing332.tts_server_android.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Entity(tableName = "readRules")
@Parcelize
@TypeConverters(ReadRule.Converters::class)
@Serializable
data class ReadRule(
    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @Transient
    var isEnabled: Boolean = false,

    var name: String = "",
    var version: Int = 0,
    var ruleId: String = "",
    var author: String = "",
    var code: String = "",

    @ColumnInfo(defaultValue = "")
    var tags: Map<String, String> = emptyMap(),

    // 索引 排序用
    @ColumnInfo(name = "order", defaultValue = "0")
    var order: Int = 0,
) : Parcelable {

    object Converters {
        @TypeConverter
        fun toTags(json: String): Map<String, String> {
            return TypeConverterUtils.decodeFromString(json) ?: emptyMap()
        }

        @TypeConverter
        fun fromTags(tags: Map<String, String>): String {
            return TypeConverterUtils.encodeToString(tags) ?: ""
        }
    }
}