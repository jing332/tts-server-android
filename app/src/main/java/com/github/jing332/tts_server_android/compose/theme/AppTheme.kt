package com.github.jing332.tts_server_android.compose.theme

import androidx.compose.ui.graphics.Color
import com.github.jing332.tts_server_android.R

enum class AppTheme(val id: String, val stringResId: Int = -1, val color: Color) {
    DEFAULT("", R.string.theme_default, green_seed),
    DYNAMIC_COLOR("dynamicColor", R.string.dynamic_color, Color.Unspecified),
    GREEN("green", R.string.green, green_seed),
    RED("red", R.string.red, red_seed),
    PINK("pink", R.string.pink, pink_seed),
    BLUE("blue", R.string.blue, blue_seed),
    CYAN("cyan", R.string.cyan, cyan_seed),
    ORANGE("orange", R.string.orange, orange_seed),
    PURPLE("purple", R.string.purple, purple_seed),
    BROWN("brown", R.string.brown, brown_seed),
    GRAY("gray", R.string.gray, gray_seed),
}