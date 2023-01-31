package com.github.jing332.tts_server_android.service

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ApiConvIntentService : IntentService("ApiConvIntentService") {
    companion object{
        const val ACTION_ON_CLOSE_SERVER = "ACTION_ON_CLOSE_SERVER"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {

    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?){

        }
    }
}