package com.github.jing332.tts_server_android

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.sysConfiguration
import java.io.File
import java.util.Locale

object AppLocale {
    val localeMap by lazy {
        linkedMapOf<String, Locale>().apply {
            BuildConfig.TRANSLATION_ARRAY.sorted().forEach {
                this[it] = Locale.forLanguageTag(it)
            }
        }
    }

    fun getLocaleCodeFromFile(context: Context): String {
        kotlin.runCatching {
            val file = File(context.filesDir.absolutePath + "/application_locale")
            return file.readText()
        }
        return ""
    }

    fun saveLocaleCodeToFile(context: Context, lang: String) {
        val file = File(context.filesDir.absolutePath + "/application_locale")
        FileUtils.saveFile(file, lang.toByteArray())
    }

    fun updateApplicationLocale(context: Context): Context {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        val targetLocale = getSetLocale(context)

        println("update locale: $targetLocale")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(targetLocale)
            configuration.setLocales(LocaleList(targetLocale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = targetLocale
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(targetLocale))
//        configuration.fontScale = getFontScale(context)
        return context.createConfigurationContext(configuration)
    }

    /**
     * 当前系统语言
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun getSystemLocale(): Locale {
        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //7.0有多语言设置获取顶部的语言
            locale = sysConfiguration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            locale = sysConfiguration.locale
        }
        return locale
    }

    /**
     * 当前App语言
     */
    @SuppressLint("ObsoleteSdkInt")
    fun getAppLocale(context: Context): Locale {
        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            locale = context.resources.configuration.locale
        }
        return locale
    }


    /**
     * 当前设置语言
     */
    private fun getSetLocale(context: Context): Locale {
        return localeMap[AppLocale.getLocaleCodeFromFile(context)] ?: getSystemLocale()
    }

    /**
     * 判断App语言和设置语言是否相同
     */
    fun isSameWithSetting(context: Context): Boolean {
        val locale = getAppLocale(context)
        val language = locale.language
        val country = locale.country
        val pfLocale = getSetLocale(context)
        val pfLanguage = pfLocale.language
        val pfCountry = pfLocale.country
        return language == pfLanguage && country == pfCountry
    }

}