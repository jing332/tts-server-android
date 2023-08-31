package com.github.jing332.tts_server_android.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Entity(tableName = "speech_rules")
@Parcelize
@TypeConverters(SpeechRule.Converters::class, MapConverters::class)
@Serializable
data class SpeechRule(
//    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = System.currentTimeMillis(),

    var isEnabled: Boolean = false,

    var name: String = "",
    var version: Int = 0,
    var ruleId: String = "",
    var author: String = "",
    var code: String = "",

    @ColumnInfo(defaultValue = "")
    var tags: Map<String, String> = mutableMapOf(),

    // 声明的tag的附加
    // 如：为tag为dialogue的声明role附加数据
    // {dialogue: {role: {label: '角色名', "hint": "仅支持前置搜索"}, } }
    @ColumnInfo(defaultValue = "")
    var tagsData: TagsDataMap = mutableMapOf(),

    // 索引 排序用
    @ColumnInfo(name = "order", defaultValue = "0")
    var order: Int = 0,
) : Parcelable {

    class Converters {
        @TypeConverter
        fun toListMap(json: String): TagsDataMap {
            return TypeConverterUtils.decodeFromString(json) ?: emptyMap()
        }

        @TypeConverter
        fun fromListMap(tags: TagsDataMap): String {
            return TypeConverterUtils.encodeToString(tags) ?: ""
        }
    }
}

// {dialogue: {role: {label: '角色名', "hint": "仅支持前置搜索"}, } }
typealias TagsDataMap =  Map<out String, Map<out String, Map<out String, String>>>