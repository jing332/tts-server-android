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

@Entity(tableName = "speech_rules")
@Parcelize
@TypeConverters(MapConverters::class)
@Serializable
data class SpeechRule(
    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = System.currentTimeMillis(),

    @Transient
    var isEnabled: Boolean = false,

    var name: String = "",
    var version: Int = 0,
    var ruleId: String = "",
    var author: String = "",
    var code: String = "",

    @ColumnInfo(defaultValue = "")
    var tags: Map<String, String> = emptyMap(),

    // 声明的tag的附加
    // 如：为tag为dialogue的声明role附加数据
    // key = dialogue, value = [role...]
    @ColumnInfo(defaultValue = "")
    var tagsData: Map<String, List<String>> = emptyMap(),

    // 索引 排序用
    @ColumnInfo(name = "order", defaultValue = "0")
    var order: Int = 0,
) : Parcelable {
}