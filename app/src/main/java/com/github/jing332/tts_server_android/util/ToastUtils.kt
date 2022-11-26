@file:Suppress("unused")
/* https://github.com/gedoor/legado/blob/master/app/src/main/java/io/legado/app/utils/ToastUtils.kt */
package com.github.jing332.tts_server_android.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

private var toast: Toast? = null

fun Context.toast(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.toast(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.longToast(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.longToast(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}


fun Fragment.toast(@StringRes message: Int) = requireActivity().toast(message)

fun Fragment.toast(message: CharSequence) = requireActivity().toast(message)

fun Fragment.longToast(@StringRes message: Int) = requireContext().longToast(message)

fun Fragment.longToast(message: CharSequence) = requireContext().longToast(message)