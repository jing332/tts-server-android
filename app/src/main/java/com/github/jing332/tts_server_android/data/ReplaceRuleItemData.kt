package com.github.jing332.tts_server_android.data

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceRuleItemData(
    var isEnabled: Boolean = true, //是否启用
    var isRegex: Boolean = false,
    var name: String, //显示名称
    var pattern: String, //匹配
    var replacement: String //替换为
) : java.io.Serializable {
}