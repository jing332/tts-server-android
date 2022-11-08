package com.github.jing332.tts_server_android.ui.custom.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

//from https://www.jianshu.com/p/a53af20c159a
class RecyclerViewAtViewPager2(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs) {
    private var startX = 0
    private var startY: Int = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
                parent.requestDisallowInterceptTouchEvent(true) //告诉viewgroup不要去拦截我
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x.toInt()
                val endY = ev.y.toInt()
                val disX = abs(endX - startX)
                val disY: Int = abs(endY - startY)
                if (disX > disY) {
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    parent.requestDisallowInterceptTouchEvent(true) //下拉的时候是false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(
                true
            )
        }
        return super.dispatchTouchEvent(ev)
    }
}