package com.github.jing332.tts_server_android.data.entities.systts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SystemTtsGroup(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "groupId")
    val id: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var isExpanded: Boolean = false,
    var name: String,
) {
    companion object {
        const val DEFAULT_GROUP_ID = 1L
    }
}
