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
            Toast.makeText(this, "服务已关闭", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            i.putExtra("isWakeLock", SharedPrefsUtils.getWakeLock(this))
            startService(i)
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

}