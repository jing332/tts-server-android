package com.github.jing332.tts_server_android.model.updater

data class UpdateResult(
    val version: String = "",
    val time: String = "",
    val content: String = "",
    val downloadUrl: String = "",
    val size: Long = 0,
) {
    fun hasUpdate() = version.isNotBlank() && downloadUrl.isNotBlank()
}