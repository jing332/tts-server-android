package com.github.jing332.tts_server_android.constant

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.github.jing332.tts_server_android.R

enum class AppTheme(
    @StyleRes val styleId: Int,
    @StringRes val strId: Int,
    @ColorRes val colorId: Int = 0
) {
    Default(R.style.Theme_TtsServer, R.string.default_str, R.color.seed),
    SkyBlue(R.style.Theme_TtsServer_SkyBlue, R.string.theme_sky_blue, R.color.sky_blue_seed),
    SorghumRed(
        R.style.Theme_TtsServer_SorghumRed,
        R.string.theme_sorghum_red,
        R.color.sorghum_red_seed
    ),
    PeacockGreen(
        R.style.Theme_TtsServer_PeacockGreen,
        R.string.theme_peacock_green,
        R.color.peacock_green_seed
    ),
}