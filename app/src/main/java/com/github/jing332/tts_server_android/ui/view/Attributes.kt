package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.util.TypedValue

object Attributes {
    fun Context.colorAttr(resId: Int): Int {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(
            resId,
            typedValue,
            true
        )
        return typedValue.data
    }

    fun Context.drawableAttr(resId: Int): Int {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(
            resId,
            typedValue,
            true
        )
        return typedValue.resourceId
    }

    val Context.colorOnBackground: Int
        get() = colorAttr(com.google.android.material.R.attr.colorOnBackground)

    val Context.selectableItemBackground: Int
        get() = drawableAttr(android.R.attr.selectableItemBackground)

    val Context.selectableItemBackgroundBorderless: Int
        get() = drawableAttr(android.R.attr.selectableItemBackgroundBorderless)

}