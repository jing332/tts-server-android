package com.github.jing332.tts_server_android.data.entities.plugin

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Plugin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var isEnabled: Boolean = false,
    var name: String = "",
    var pluginId: String = "",
    var author: String = "",
    var code: String = "",

    ) : Parcelable {
    override fun toString(): String {
        return "name: $name, pluginId: $pluginId, author: $author, isEnabled: $isEnabled"
    }
}