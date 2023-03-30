package com.github.jing332.tts_server_android.data.entities.plugin

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Parcelize
@Entity
data class Plugin(
    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @Transient
    var isEnabled: Boolean = false,

    @ColumnInfo(name = "version", defaultValue = "0")
    var version: Int = 0,

    var name: String = "",
    var pluginId: String = "",
    var author: String = "",
    var code: String = "",

    // 索引 排序用
    @ColumnInfo(name = "order", defaultValue = "0")
    var order: Int = 0,
) : Parcelable {
    override fun toString(): String {
        return "name: $name, pluginId: $pluginId, author: $author, version: $version, isEnabled: $isEnabled"
    }
}