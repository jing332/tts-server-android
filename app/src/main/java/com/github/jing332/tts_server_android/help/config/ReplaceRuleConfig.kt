package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel

object ReplaceRuleConfig : KotprefModel() {
    override val kotprefName: String
        get() = "replace_rule"


    val defSymbols = listOf(
        "(",
        ")",
        "[",
        "]",
        "|",
        "\\",
        "/",
        "{",
        "}",
        "^",
        "$",
        ".",
        "*",
        "+",
        "?"
    ).joinToString("\n")

    var symbols by stringPref(defSymbols)
}