package com.github.jing332.tts_server_android.bean


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AzureVoiceBean(
    @SerialName("Gender")
    val gender: String,
    @SerialName("DisplayName")
    val displayName: String,
    @SerialName("LocalName")
    val localName: String,
    @SerialName("Locale")
    val locale: String,
    @SerialName("LocaleName")
    val localeName: String,
    @SerialName("Name")
    val name: String,
    @SerialName("RolePlayList")
    val rolePlayList: List<String>?,
    @SerialName("ShortName")
    val shortName: String,
    @SerialName("StyleList")
    val styleList: List<String>?,

    @SerialName("SecondaryLocaleList")
    val secondaryLocaleList: List<String>?
//    @SerialName("SampleRateHertz")
//    val sampleRateHertz: String,
//    @SerialName("Status")
//    val status: String,
//    @SerialName("VoiceType")
//    val voiceType: String,
//    @SerialName("WordsPerMinute")
//    val wordsPerMinute: String
)