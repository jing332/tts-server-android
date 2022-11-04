package com.github.jing332.tts_server_android.data

import java.io.Serializable

// 列表UI显示用Data类
data class TtsConfigListItemData(var displayName: String, var content: String) : Serializable {
    constructor() : this("", "")
}