package com.github.jing332.tts_server_android.ui.view.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object DataBindingAdapters {
    @BindingAdapter("imgRes")
    @JvmStatic
    fun setImageResource(imageView: ImageView, drawable: Drawable) {
        imageView.setImageDrawable(drawable)
    }

    @BindingAdapter("visible")
    @JvmStatic
    fun View.setVisible(show: Boolean) {
        visibility = if (show) VISIBLE else GONE
    }
}