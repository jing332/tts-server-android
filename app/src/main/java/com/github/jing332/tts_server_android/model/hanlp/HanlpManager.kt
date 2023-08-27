package com.github.jing332.tts_server_android.model.hanlp

import android.util.Log
import com.hankcs.hanlp.HanLP
import java.io.File

object HanlpManager {
    const val TAG = "HanlpManager"

    fun test(): Boolean {
        kotlin.runCatching {
            HanLP.newSegment().seg("test, 测试")
            return true
        }

        return false
    }

    fun initDir(dir: String) {
        val cfgClz = HanLP.Config::class.java
        for (field in cfgClz.declaredFields) {
            if (field.type == String::class.java) {
                field.isAccessible = true
                val value = field.get(null) as String
                val newValue = dir + File.separator + value.removePrefix("data/")
                field.set(null, newValue)
                Log.d(TAG, "set config: $newValue")
            }
        }
    }
}