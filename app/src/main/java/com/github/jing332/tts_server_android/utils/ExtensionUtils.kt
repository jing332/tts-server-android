package com.github.jing332.tts_server_android.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
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

val WindowManager.windowSize: DisplayMetrics
    get() {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            displayMetrics.widthPixels = windowMetrics.bounds.width() - insets.left - insets.right
            displayMetrics.heightPixels = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            defaultDisplay.getMetrics(displayMetrics)
        }
        return displayMetrics
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

/**
 * 绑定返回键回调（建议使用该方法）
 * @param owner Receive callbacks to a new OnBackPressedCallback when the given LifecycleOwner is at least started.
 * This will automatically call addCallback(OnBackPressedCallback) and remove the callback as the lifecycle state changes. As a corollary, if your lifecycle is already at least started, calling this method will result in an immediate call to addCallback(OnBackPressedCallback).
 * When the LifecycleOwner is destroyed, it will automatically be removed from the list of callbacks. The only time you would need to manually call OnBackPressedCallback.remove() is if you'd like to remove the callback prior to destruction of the associated lifecycle.
 * @param onBackPressed 回调方法；返回true则表示消耗了按键事件，事件不会继续往下传递，相反返回false则表示没有消耗，事件继续往下传递
 * @return 注册的回调对象，如果想要移除注册的回调，直接通过调用[OnBackPressedCallback.remove]方法即可。
 */
fun androidx.activity.ComponentActivity.addOnBackPressed(
    owner: LifecycleOwner,
    onBackPressed: () -> Boolean
): OnBackPressedCallback {
    return backPressedCallback(onBackPressed).also {
        onBackPressedDispatcher.addCallback(owner, it)
    }
}

/**
 * 绑定返回键回调，未关联生命周期，建议使用关联生命周期的办法（尤其在fragment中使用，应该关联fragment的生命周期）
 */
fun androidx.activity.ComponentActivity.addOnBackPressed(onBackPressed: () -> Boolean): OnBackPressedCallback {
    return backPressedCallback(onBackPressed).also {
        onBackPressedDispatcher.addCallback(it)
    }
}

private fun androidx.activity.ComponentActivity.backPressedCallback(onBackPressed: () -> Boolean): OnBackPressedCallback {
    return object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!onBackPressed()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
}