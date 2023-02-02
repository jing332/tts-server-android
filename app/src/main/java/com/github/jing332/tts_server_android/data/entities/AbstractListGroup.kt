package com.github.jing332.tts_server_android.data.entities

interface AbstractListGroup {
    val id: Long
    var name: String
    var order: Int
    var isExpanded: Boolean

    companion object {
        const val DEFAULT_GROUP_ID = 1L
    }
}
