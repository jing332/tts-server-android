package com.github.jing332.tts_server_android.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.constant.KeyConst
import java.lang.reflect.ParameterizedType


fun Uri.grantReadWritePermission(contentResolver: ContentResolver) {
    contentResolver.takePersistableUriPermission(
        this,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
}

fun Intent.getBinder(): IBinder? {
    val bundle = getBundleExtra(KeyConst.KEY_BUNDLE)
    return bundle?.getBinder(KeyConst.KEY_LARGE_DATA_BINDER)
}

fun Intent.setBinder(binder: IBinder) {
    putExtra(
        KeyConst.KEY_BUNDLE,
        Bundle().apply {
            putBinder(KeyConst.KEY_LARGE_DATA_BINDER, binder)
        })
}

val Int.dp: Int get() = SizeUtils.dp2px(this.toFloat())

val Int.px: Int get() = SizeUtils.px2dp(this.toFloat())

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun ViewGroup.setMarginMatchParent() {
    this.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
}

@Suppress("UNCHECKED_CAST")
fun <T : ViewBinding> Any.inflateBinding(
    inflater: LayoutInflater,
    root: ViewGroup? = null,
    attachToParent: Boolean = false
): T {
    return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        .filterIsInstance<Class<T>>()
        .first()
        .getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        .also { it.isAccessible = true }
        .invoke(null, inflater, root, attachToParent) as T
}

@Suppress("DEPRECATION")
val Activity.displayHeight: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            windowMetrics.bounds.height() - insets.bottom - insets.top
        } else
            windowManager.defaultDisplay.height
    }

/**
 * 点击防抖动
 */
fun View.clickWithThrottle(throttleTime: Long = 600L, action: (v: View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < throttleTime) return
            else action(v)

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun ViewPager2.reduceDragSensitivity(f: Int = 4) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * f)       // "8" was obtained experimentally
}