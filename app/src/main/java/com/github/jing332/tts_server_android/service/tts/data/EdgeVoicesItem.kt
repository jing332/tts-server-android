package com.github.jing332.tts_server_android.service.tts.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EdgeVoicesItem (
    @SerialName("FriendlyName")
    val friendlyName: String,
    @SerialName("Locale")
    val locale: String,
    @SerialName("Name")
    val name: String,
    @SerialName("ShortName")
    val shortName: String,
//    @SerialName("Gender")
//    val gender: String,
//    @SerialName("Status")
//    val status: String,
//    @SerialName("SuggestedCodec")
//    val suggestedCodec: String,
)