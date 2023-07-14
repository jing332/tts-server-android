package com.github.jing332.tts_server_android.help

import com.github.jing332.tts_server_android.constant.ConfigType

object ConfigImportHelper {
    private val fieldMap = mapOf(
        "list" to ConfigType.LIST,
        "pluginId" to ConfigType.PLUGIN,
        "pattern" to ConfigType.REPLACE_RULE,
        "ruleId" to ConfigType.SPEECH_RULE
    )

    /**
     * 检查配置字符串是否符合配置格式
     */
    fun getConfigType(str: String): ConfigType {
        fieldMap.forEach {
            if (str.contains("\"${it.key}\":")) {
                return it.value
            }
        }

        return ConfigType.UNKNOWN
    }
}