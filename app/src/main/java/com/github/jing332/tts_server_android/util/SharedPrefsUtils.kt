package com.github.jing332.tts_server_android.util

import android.content.Context
import androidx.preference.PreferenceManager

object SharedPrefsUtils {
    fun getWakeLock(ctx: Context): Boolean {
        val pref = ctx.getSharedPreferences("config", Context.MODE_PRIVATE)
        return pref.getBoolean("wakeLock", false)
    }

    fun setWakeLock(ctx: Context, isWakeLock: Boolean) {
        val editor = ctx.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
        editor.putBoolean("wakeLock", isWakeLock)
        editor.apply()
    }

    fun setToken(ctx: Context, token: String) {
        val editor = ctx.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
        editor.putString("token", token)
        editor.apply()
    }

    fun getToken(ctx: Context): String {
        val pref = ctx.getSharedPreferences("config", Context.MODE_PRIVATE)
        return pref.getString("token", "").toString()
    }

    fun setPort(ctx: Context, port: Int) {
        val editor = ctx.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
        editor.putInt("port", port)
        editor.apply()
    }

    fun getPort(ctx: Context): Int {
        val pref = ctx.getSharedPreferences("config", Context.MODE_PRIVATE)
        return pref.getInt("port", 1233)
    }


    fun getUseDnsEdge(ctx: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
        return pref.getBoolean("useDnsEdge", false)
    }
}