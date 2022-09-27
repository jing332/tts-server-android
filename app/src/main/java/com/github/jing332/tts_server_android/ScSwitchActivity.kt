package com.github.jing332.tts_server_android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScSwitchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* 隐藏Activity */
        moveTaskToBack(true)
        setContentView(R.layout.activity_none)

        if (TtsIntentService.IsRunning) {
            TtsIntentService.closeServer(this)
            Toast.makeText(this, "服务已关闭", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            startService(i)
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

}