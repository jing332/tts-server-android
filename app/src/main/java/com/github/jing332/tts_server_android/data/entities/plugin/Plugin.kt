package com.github.jing332.tts_server_android.data.entities.plugin

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.jing332.tts_server_android.data.entities.MapConverters
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Parcelize
@Entity
@TypeConverters(MapConverters::class)
data class Plugin(
    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var isEnabled: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    var version: Int = 0,
    var name: String = "",
    var pluginId: String = "",
    var author: String = "",
    var code: String = "",

    // authKey: { label: "验证KEY", hint: "填入用于验证身份的KEY"}
    @ColumnInfo(name = "defVars", defaultValue = "{}")
    var defVars: Map<String, Map<String, String>> = mutableMapOf(),


    @ColumnInfo(defaultValue = "{}")
    var userVars: Map<String, String> = mutableMapOf(),

    // 索引 排序用
    @ColumnInfo(name = "order", defaultValue = "0")
    var order: Int = 0,
) : Parcelable {
    val mutableUserVars: MutableMap<String, String>
        get() = userVars as MutableMap<String, String>

    override fun toString(): String {
        return "name: $name, pluginId: $pluginId, author: $author, version: $version, isEnabled: $isEnabled"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is Plugin && other.userVars == userVars
    }
}