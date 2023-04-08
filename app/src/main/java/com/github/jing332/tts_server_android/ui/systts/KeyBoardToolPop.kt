package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.github.jing332.tts_server_android.util.windowSize
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import splitties.systemservices.windowManager
import kotlin.math.abs

class KeyBoardToolPop(
    val context: Context,
    private val rootView: View,
    private val customView: View? = null,
) :
    PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    ViewTreeObserver.OnGlobalLayoutListener {

    private var mIsSoftKeyBoardShowing: Boolean = false
    private val rv by lazy { RecyclerView(context) }
    private val chipGroup by lazy { ChipGroup(context) }

    init {
        if (customView == null) {
            contentView = chipGroup
            chipGroup.addView(Chip(context).apply { text = "12222" })
        } else {
            contentView = customView
        }

//        contentView.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            100.dp
//        )


        isTouchable = true
        isOutsideTouchable = false
        isFocusable = false
        inputMethodMode = INPUT_METHOD_NEEDED // 避免遮盖输入法
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