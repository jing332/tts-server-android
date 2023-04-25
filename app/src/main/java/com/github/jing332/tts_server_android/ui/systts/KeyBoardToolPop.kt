package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.graphics.Rect
import android.view.*
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.github.jing332.tts_server_android.ui.view.Attributes.colorOnBackground
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.windowSize
import splitties.systemservices.windowManager
import kotlin.math.abs

class KeyBoardToolPop(
    val context: Context,
    private val rootView: View,
    customView: View? = null,
) : PopupWindow(
    customView,
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
), ViewTreeObserver.OnGlobalLayoutListener {

    private var mIsSoftKeyBoardShowing: Boolean = false

    init {
        isTouchable = true
        isOutsideTouchable = false
        isFocusable = false
        inputMethodMode = INPUT_METHOD_NEEDED // 避免遮盖输入法
//        setBackgroundDrawable(ContextCompat.getDrawable(context, context.colorOnBackground))
    }

    fun attachToWindow(window: Window) {
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(this)
        contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED,
        )
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
            rootView.setPadding(0, 0, 0, contentView.measuredHeight)
            if (!isShowing)
                showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
        } else {
            mIsSoftKeyBoardShowing = false
            rootView.setPadding(0, 0, 0, 0)
            if (preShowing) {
                dismiss()
            }
        }
    }


}