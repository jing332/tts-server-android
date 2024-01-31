package com.github.jing332.tts_server_android.data.entities.replace

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "replaceRule")
data class ReplaceRule(
    @PrimaryKey(autoGenerate = true)
    var id: Long = System.currentTimeMillis(),

    // 所属组的ID
    @ColumnInfo(defaultValue = DEFAULT_GROUP_ID.toString())
    var groupId: Long = DEFAULT_GROUP_ID,

    // 显示名称
    var name: String = "",
    // 是否启用
    var isEnabled: Boolean = true,
    // 是否正则
    var isRegex: Boolean = false,
    // 匹配
    var pattern: String = "",
    // 替换为
    var replacement: String = "",
    // 索引 排序用
    @ColumnInfo(defaultValue = "0")
    var order: Int = 0,

    @ColumnInfo(defaultValue = "")
    var sampleText: String = ""
) : Parcelable