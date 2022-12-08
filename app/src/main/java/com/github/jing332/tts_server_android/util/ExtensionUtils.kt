package com.github.jing332.tts_server_android.util

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R

fun ViewPager2.reduceDragSensitivity(f: Int = 4) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * f)       // "8" was obtained experimentally
}

/*fun AlertDialog.setFadeAnim(): AlertDialog {
    this.window?.setWindowAnimations(R.style.dialogFadeStyle)
    return this
}*/

fun AlertDialog.Builder.setFadeAnim(): AlertDialog {
    return create().apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }
}