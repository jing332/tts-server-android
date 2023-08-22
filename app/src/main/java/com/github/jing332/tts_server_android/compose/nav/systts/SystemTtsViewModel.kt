package com.github.jing332.tts_server_android.compose.nav.systts

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import kotlinx.serialization.encodeToString

class SystemTtsViewModel : ViewModel() {
    fun exportConfig(): String {
        return try {
            AppConst.jsonBuilder.encodeToString(appDb.systemTtsDao.getSysTtsWithGroups())
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }
}