package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import com.github.jing332.tts_server_android.utils.runOnUI

object AppDialogs {
    fun Context.displayErrorDialog(t: Throwable, title: String? = null) {
        runOnUI {
            ErrorDialogActivity.start(this, title ?: "Error", t)
        }
    }
}