package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib

class ReplaceManagerViewModel : ViewModel() {
    fun exportConfig(): String {
        return App.jsonBuilder.encodeToString(appDb.replaceRuleDao.all)
    }

    fun uploadConfigToUrl(cfg: String): Result<String> {
        return try {
            Result.success(Tts_server_lib.uploadConfig(cfg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importConfig(jsonStr: String): String? {
        try {
            App.jsonBuilder.decodeFromString<List<ReplaceRule>>(jsonStr).forEach {
                appDb.replaceRuleDao.insert(it)
            }
        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    fun importConfigFromUrl(url: String): String? {
        return try {
            importConfig(Tts_server_lib.httpGet(url, "").decodeToString())
        } catch (e: Exception) {
            e.message
        }
    }
}