@file:Suppress("unused")
/* https://github.com/gedoor/legado/blob/master/app/src/main/java/io/legado/app/utils/ToastUtils.kt */
package com.github.jing332.tts_server_android.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

private var toast: Toast? = null

fun Context.toastOnUi(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.toastOnUi(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.longToastOnUi(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.longToastOnUi(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}


fun Fragment.toastOnUi(@StringRes message: Int) = requireActivity().toastOnUi(message)

fun Fragment.toastOnUi(message: CharSequence) = requireActivity().toastOnUi(message)

fun Fragment.longToast(@StringRes message: Int) = requireContext().longToastOnUi(message)

fun Fragment.longToast(message: CharSequence) = requireContext().longToastOnUi(message)