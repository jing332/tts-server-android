package com.github.jing332.tts_server_android.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import java.util.*


object ApplicationUtils {
    /**
     * 获取应用支持的语言
     */
    @Suppress("DEPRECATION")
    fun getAppLanguages(ctx: Context, id: Int): Set<String> {
        val dm: DisplayMetrics = ctx.resources.displayMetrics
        val conf: Configuration = ctx.resources.configuration
        val originalLocale: Locale = conf.locale
        conf.locale = Locale.ENGLISH
        val reference: String = Resources(ctx.assets, dm, conf).getString(id)
        val result: MutableSet<String> = HashSet()
        result.add(Locale.ENGLISH.language)
        for (loc in ctx.assets.locales) {
            if (loc.isEmpty()) continue
            val l: Locale = Locale.forLanguageTag(loc)
            conf.locale = l
            if (reference != Resources(ctx.assets, dm, conf).getString(id)) result.add(l.language)
        }
        conf.locale = originalLocale
        return result
    }
}