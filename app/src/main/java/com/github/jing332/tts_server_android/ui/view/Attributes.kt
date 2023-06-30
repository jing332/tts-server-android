package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

object Attributes {
    @ColorInt
    fun Context.colorAttr(resId: Int): Int {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(
            resId,
            typedValue,
            true
        )
        return typedValue.data
    }


    @DrawableRes
    fun Context.drawableAttr(resId: Int): Int {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(
            resId,
            typedValue,
            true
        )
        return typedValue.resourceId
    }

    @get:ColorInt
    val Context.colorChipStroke: Int
        get() = colorAttr(com.google.android.material.R.attr.chipStrokeColor)

    @get:ColorInt
    val Context.colorControlHighlight: Int
        get() = colorAttr(com.google.android.material.R.attr.colorControlHighlight)

    @get:ColorInt
    val Context.colorSurface: Int
        get() = colorAttr(com.google.android.material.R.attr.colorSurface)

    @get:ColorInt
    val Context.colorOnBackground: Int
        get() = colorAttr(com.google.android.material.R.attr.colorOnBackground)

    @get:DrawableRes
    val Context.selectableItemBackground: Int
        get() = drawableAttr(android.R.attr.selectableItemBackground)

    @get:DrawableRes
    val Context.selectableItemBackgroundBorderless: Int
        get() = drawableAttr(android.R.attr.selectableItemBackgroundBorderless)

}