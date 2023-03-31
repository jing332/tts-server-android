package com.github.jing332.tts_server_android.data.entities.systts

import androidx.room.Embedded
import androidx.room.Relation

@kotlinx.serialization.Serializable
data class GroupWithSystemTts(
    @Embedded
    val group: SystemTtsGroup,

    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId"
    )
    val list: List<SystemTts>
)