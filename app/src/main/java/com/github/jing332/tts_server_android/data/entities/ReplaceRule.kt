package com.github.jing332.tts_server_android.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "replaceRule")
data class ReplaceRule(
    @PrimaryKey(autoGenerate = true)
    var id: Long = System.currentTimeMillis(),

    // 显示名称
    var name: String,
    // 是否启用
    var isEnabled: Boolean = true,
    // 是否正则
    var isRegex: Boolean = false,
    // 匹配
    var pattern: String,
    // 替换为
    var replacement: String,
    // 索引 排序用
    @ColumnInfo(defaultValue = "0")
    var order: Int = 0,
) : Parcelable