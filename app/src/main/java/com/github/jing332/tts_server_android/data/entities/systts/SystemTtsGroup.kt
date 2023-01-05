package com.github.jing332.tts_server_android.data.entities.systts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity
data class SystemTtsGroup(
    @PrimaryKey
    @ColumnInfo(name = "groupId")
    val id: Long = System.currentTimeMillis(),

    var name: String,

    @ColumnInfo(defaultValue = "0")
    var order: Int = 0,

    @kotlinx.serialization.Transient
    var isExpanded: Boolean = false,
) {
    companion object {
        const val DEFAULT_GROUP_ID = 1L
    }
}
