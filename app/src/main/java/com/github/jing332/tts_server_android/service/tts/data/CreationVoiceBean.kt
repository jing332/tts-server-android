package com.github.jing332.tts_server_android.service.tts.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreationVoiceBean(
    @SerialName("id") val id: String,
    @SerialName("locale") val locale: String,
    @SerialName("name") val name: String,
    @SerialName("properties") val properties: Properties,
    @SerialName("shortName") val shortName: String,
    @SerialName("voiceType") val voiceType: String
//    @SerialName("categories")
//    val categories: List<Any>,
//    @SerialName("masterpieces")
//    val masterpieces: List<Any>,
//    @SerialName("samples")
//    val samples: Samples,
)

@Serializable
data class Properties(
    @SerialName("DisplayName") val displayName: String,
    @SerialName("DisplayVoiceName") val displayVoiceName: String,
    @SerialName("LocalName") val localName: String,
    @SerialName("LocaleDescription") val localeDescription: String,
    @SerialName("VoiceRoleNames") val voiceRoleNames: String,
    @SerialName("VoiceStyleNames") val voiceStyleNames: String

//    @SerialName("ExpressAsRoleIdDefinitions")
//    val expressAsRoleIdDefinitions: String,
//    @SerialName("ExpressAsStyleIdDefinitions")
//    val expressAsStyleIdDefinitions: String,
//    @SerialName("FrontendVoiceType")
//    val frontendVoiceType: String,
//    @SerialName("Gender")
//    val gender: String,
//    @SerialName("VoiceStyleNameDefinitions")
//    val voiceStyleNameDefinitions: String,
//    @SerialName("PreviewSentence")
//    val previewSentence: String,
//    @SerialName("ReleaseScope")
//    val releaseScope: String,
//    @SerialName("SampleRateHertz")
//    val sampleRateHertz: String,
//    @SerialName("SecondaryLocales")
//    val secondaryLocales: String,
//    @SerialName("ShortName")
//    val shortName: String,
//    @SerialName("VoiceModelKind")
//    val voiceModelKind: String,
//    @SerialName("VoiceRoleNameDefinitions")
//    val voiceRoleNameDefinitions: String,
)
//
//@Serializable
//data class Samples(
//    @SerialName("languageSamples")
//    val languageSamples: List<Any>,
//    @SerialName("roleSamples")
//    val roleSamples: List<Any>,
//    @SerialName("styleSamples")
//    val styleSamples: List<StyleSample>
//)
//
//@Serializable
//data class StyleSample(
//    @SerialName("audioFileEndpointWithSas")
//    val audioFileEndpointWithSas: String,
//    @SerialName("styleName")
//    val styleName: String
//)