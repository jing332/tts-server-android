package com.github.jing332.tts_server_android.ui.systts.replace

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
class KeyBoardHelper(private val rootView: View) : ViewTreeObserver.OnGlobalLayoutListener {

    private var mIsSoftKeyBoardShowing = false
    private var isShowing = false

    interface Callback {
        fun onShow()
        fun onDismiss()
    }

    var callback: Callback? = null

    fun attachToWindow(window: Window) {
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(this)
//        contentView.measure(
//            View.MeasureSpec.UNSPECIFIED,
//            View.MeasureSpec.UNSPECIFIED,
//        )
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
//            rootView.setPadding(0, 0, 0, contentView.measuredHeight)
            if (!isShowing)
                callback?.onShow()
//                showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
        } else {
            mIsSoftKeyBoardShowing = false
            rootView.setPadding(0, 0, 0, 0)
            if (preShowing) {
                callback?.onDismiss()
//                dismiss()
            }
        }
    }


}