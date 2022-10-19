package com.github.jing332.tts_server_android.utils

import android.os.SystemClock


object GcManager {
    var last: Long = 0

    /**
     * 避免频繁GC
     */
    @Synchronized
    fun doGC() {
        if (SystemClock.elapsedRealtime() - last > 10000) {
            Runtime.getRuntime().gc()
            last = SystemClock.elapsedRealtime()
        }

    }

}
