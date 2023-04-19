package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.util.TypedValue

object Attr {
    fun Context.colorAttr(resId: Int): Int {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(
            resId,
            typedValue,
            true
        )
        return typedValue.data
    }

    val Context.colorOnBackground: Int
        get() = colorAttr(com.google.android.material.R.attr.colorOnBackground)

}