package com.github.jing332.tts_server_android.help

import com.github.jing332.tts_server_android.constant.ConfigType

object ConfigImportHelper {
    private val fieldMap = mapOf(
        listOf("pluginId", "code", "version") to ConfigType.PLUGIN,
        "pattern" to ConfigType.REPLACE_RULE,
        listOf("ruleId", "tags") to ConfigType.SPEECH_RULE,
        listOf("list", "tts", "displayName", "#type") to ConfigType.LIST,
    )

    /**
     * 检查配置字符串是否符合配置格式
     */
    fun getConfigType(str: String): ConfigType {
        fieldMap.forEach { entry ->
            when (entry.key) {
                is String -> if (str.contains("\"${entry.key}\":")) {
                    return entry.value
                }

                is List<*> -> if (str.contains((entry.key as List<*>).map { "\"${it.toString()}\":" })) {
                    return entry.value
                }
            }
        }

        return ConfigType.UNKNOWN
    }

    private fun String.contains(list: List<String>): Boolean {
        var count = 0
        list.forEach {
            if (this.contains(it)) count++
        }
        return count == list.size
    }
}