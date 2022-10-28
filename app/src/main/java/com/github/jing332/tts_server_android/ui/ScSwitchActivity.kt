package com.github.jing332.tts_server_android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.service.TtsIntentService
import com.github.jing332.tts_server_android.utils.SharedPrefsUtils

/* 桌面长按菜单{开关} */
class ScSwitchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)
        if (TtsIntentService.instance?.isRunning == true)
            TtsIntentService.instance?.closeServer()
        else
            startService(Intent(this, TtsIntentService::class.java))

        finish()
    }
}