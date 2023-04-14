package com.github.jing332.tts_server_android.data.entities

import androidx.room.TypeConverter

object MapConverters {
    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        return TypeConverterUtils.decodeFromString(json) ?: emptyMap()
    }

    @TypeConverter
    fun fromMap(tags: Map<String, String>): String {
        return TypeConverterUtils.encodeToString(tags) ?: ""
    }

    @TypeConverter
    fun toNestMap(json: String): Map<String, Map<String, String>> {
        return TypeConverterUtils.decodeFromString(json) ?: emptyMap()
    }

    @TypeConverter
    fun fromNestMap(map: Map<String, Map<String, String>>): String {
        return TypeConverterUtils.encodeToString(map) ?: ""
    }

    @TypeConverter
    fun toMapList(json: String): Map<String, List<Map<String, String>>> {
        return TypeConverterUtils.decodeFromString(json) ?: emptyMap()
    }

    @TypeConverter
    fun fromMapList(tags: Map<String, List<Map<String, String>>>): String {
        return TypeConverterUtils.encodeToString(tags) ?: ""
    }
}