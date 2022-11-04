package com.github.jing332.tts_server_android.service.systts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech

class CheckVoiceData : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = TextToSpeech.Engine.CHECK_VOICE_DATA_PASS
        val returnData = Intent()

        val available: ArrayList<String> = arrayListOf("zho-CHN")
        val unavailable: ArrayList<String> = arrayListOf()

        returnData.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available)
        returnData.putStringArrayListExtra(
            TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES,
            unavailable
        )
        setResult(result, returnData)
        finish()
    }
}