package com.github.jing332.tts_server_android.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseApiBean(
    @SerialName("assets")
    val assets: List<Asset>,
    @SerialName("body")
    val body: String,
    @SerialName("tag_name")
    val tagName: String,
)

@Serializable
data class Asset(
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("name")
    val name: String,
    @SerialName("size")
    val size: Int,
)