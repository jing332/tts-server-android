@file:Suppress("unused")

package com.github.jing332.tts_server_android.utils

import android.content.res.Configuration
import android.content.res.Resources

val sysConfiguration: Configuration = Resources.getSystem().configuration

val Configuration.isNightMode: Boolean
    get() {
        val mode = uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }