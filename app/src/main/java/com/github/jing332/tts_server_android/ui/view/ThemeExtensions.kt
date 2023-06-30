package com.github.jing332.tts_server_android.ui.view

import android.app.Activity
import com.github.jing332.tts_server_android.constant.AppTheme
import com.github.jing332.tts_server_android.help.config.AppConfig

object ThemeExtensions {
    /**
     * 初始化主题
     */
    fun Activity.initAppTheme() {
        kotlin.runCatching {
            setTheme(AppConfig.theme.styleId)
        }.onFailure {
            AppConfig.theme = AppTheme.Default
            setTheme(AppConfig.theme.styleId)
        }
    }
}