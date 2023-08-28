package com.github.jing332.tts_server_android.compose.systts.list

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import kotlinx.serialization.encodeToString

class ListManagerViewModel : ViewModel() {
    val audioPlayer by lazy { AudioPlayer(app) }

    fun export(): Result<String> {
        return try {
            Result.success(AppConst.jsonBuilder.encodeToString(appDb.systemTtsDao.getSysTtsWithGroups()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}