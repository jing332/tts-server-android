package com.github.jing332.tts_server_android

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.github.jing332.tts_server_android.utils.FileUtils
import com.github.jing332.tts_server_android.utils.sysConfiguration
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
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

    fun setLocale(context: Context, locale: Locale = getLocaleFromFile(context)) {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        val newLocale = getLocaleFromFile(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale)
            val localeList = LocaleList(newLocale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            Locale.setDefault(newLocale)
            configuration.setLocale(newLocale)
        }

        resources.updateConfiguration(configuration, metrics)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
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
    fun getLocaleFromFile(context: Context): Locale {
        return localeMap[getLocaleCodeFromFile(context)] ?: getSystemLocale()
    }

    /**
     * 判断App语言和设置语言是否相同
     */
    fun isSameWithSetting(context: Context): Boolean {
        val locale = getAppLocale(context)
        val language = locale.language
        val country = locale.country
        val pfLocale = getLocaleFromFile(context)
        val pfLanguage = pfLocale.language
        val pfCountry = pfLocale.country
        return language == pfLanguage && country == pfCountry
    }

}