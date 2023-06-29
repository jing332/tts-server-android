package com.github.jing332.tts_server_android.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LegadoHttpTts(
    @SerialName("header") val header: String = "",
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String = "",
    @SerialName("url") val url: String = ""

//    @SerialName("concurrentRate")
//    val concurrentRate: String,
//    @SerialName("contentType")
//    val contentType: String,
//    @SerialName("enabledCookieJar")
//    val enabledCookieJar: Boolean,
//    @SerialName("lastUpdateTime")
//    val lastUpdateTime: Long,
//    @SerialName("loginCheckJs")
//    val loginCheckJs: String,
//    @SerialName("loginUi")
//    val loginUi: String,
//    @SerialName("loginUrl")
//    val loginUrl: String,
)