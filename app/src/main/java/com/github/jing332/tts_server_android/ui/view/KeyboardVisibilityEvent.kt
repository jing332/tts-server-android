package com.github.jing332.tts_server_android.ui.view

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import com.github.jing332.tts_server_android.utils.windowSize
import splitties.systemservices.windowManager
import kotlin.math.abs

/**
 * 用于监听软键盘的弹出和隐藏
 */
class KeyboardVisibilityEvent(private val rootView: View, var callback: Callback) :
    ViewTreeObserver.OnGlobalLayoutListener {

    private var mIsSoftKeyBoardShowing = false
    private var isShowing = false

    fun interface Callback {
        fun onChanged(isVisible: Boolean)
    }

    fun attachToWindow(window: Window) {
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val rect = Rect()
        // 获取当前页面窗口的显示范围
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = windowManager.windowSize.heightPixels
        val keyboardHeight = screenHeight - rect.bottom // 输入法的高度
        val preShowing = mIsSoftKeyBoardShowing
        if (abs(keyboardHeight) > screenHeight / 5) {
            mIsSoftKeyBoardShowing = true // 超过屏幕五分之一则表示弹出了输入法
            if (!isShowing) callback.onChanged(true)
        } else {
            mIsSoftKeyBoardShowing = false
            rootView.setPadding(0, 0, 0, 0)
            if (preShowing) callback.onChanged(false)
        }
    }
}