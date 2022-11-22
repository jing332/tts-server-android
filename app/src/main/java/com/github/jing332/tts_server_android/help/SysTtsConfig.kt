package com.github.jing332.tts_server_android.help

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.PreferKey

object SysTtsConfig {
    var isMultiVoiceEnabled: Boolean
        get() {
            return pull(PreferKey.isMultiVoiceEnabled, false)
        }
        set(value) {
            push(PreferKey.isMultiVoiceEnabled, value)
        }

    var isReplaceEnabled: Boolean
        get() {
            return App.prefs.pull(PreferKey.isReplaceEnabled, false)
        }
        set(value) {
            push(PreferKey.isReplaceEnabled, value)
        }

    var isSplitEnabled: Boolean
        get() {
            return pull(PreferKey.isSplitEnabled, false)
        }
        set(value) {
            push(PreferKey.isSplitEnabled, value)
        }

    var requestTimeout: Int
        get() {
            return pull(PreferKey.requestTimeout, 5)
        }
        set(value) {
            push(PreferKey.requestTimeout, value)
        }

    var minDialogueLength: Int
        get() {
            return pull(PreferKey.minDialogueLength, 0)
        }
        set(value) {
            push(PreferKey.minDialogueLength, value)
        }

    private inline fun <reified T : Any> pull(key: String, default: T): T {
        return App.prefs.pull(key, default)
    }

    private fun <T : Any> push(
        key: String,
        value: T
    ): Unit = App.prefs.push(
        key, value
    )
}

