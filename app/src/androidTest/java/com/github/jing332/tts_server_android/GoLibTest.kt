package com.github.jing332.tts_server_android

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class GoLibTest {
    @Test
    fun goLib() {
        val arg = tts_server_lib.CreationArg()
        arg.text = "test"
        arg.voiceName = "en-US-AIGenerate1Neural"
        arg.voiceId = "5120f8b71-e1cc-4e80-b9ea-006d2f816864"
        arg.style = "general"
        arg.styleDegree = "1.0"
        arg.role = "default"
        arg.volume = "0%"
        arg.format = "audio-16khz-32kbitrate-mono-mp3"

        try {
            val audio = tts_server_lib.Tts_server_lib.getCreationAudio(arg)
            Log.e("TestGo", audio.contentToString())
        } catch (e: Exception) {
            Log.e("TestGo", e.message.toString())
            e.printStackTrace()
        }

    }
}