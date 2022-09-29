package com.github.jing332.tts_server_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ScSwitchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)

        if (TtsIntentService.IsRunning) {
            TtsIntentService.closeServer(this)
        } else {
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            i.putExtra("isWakeLock", SharedPrefsUtils.getWakeLock(this))
            startService(i)
        }

        finish()
    }

}