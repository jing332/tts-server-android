package com.github.jing332.tts_server_android.constant

import androidx.annotation.StringRes
import com.github.jing332.tts_server_android.R

enum class ConfigType(@StringRes val strId: Int) {
    UNKNOWN(R.string.unknown),
    LIST(R.string.config_list),
    PLUGIN(R.string.plugin),
    REPLACE_RULE(R.string.replace_rule),
    SPEECH_RULE(R.string.speech_rule);
}