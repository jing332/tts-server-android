package com.github.jing332.tts_server_android.ui.systts

import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.github.jing332.tts_server_android.R
import splitties.systemservices.accessibilityManager

object BrvItemTouchHelper {


    /**
     * 带分组的item拖拽
     * @param T 分组Model
     * @return 是否可以拖拽
     */
    inline fun <reified T> onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val src = source as BindingAdapter.BindingViewHolder
        val tgt = target as BindingAdapter.BindingViewHolder
        val isGroup = src.getModelOrNull<T>() != null

//        Log.e(TAG, "${src.findParentPosition()} - ${tgt.findParentPosition()}")
        // 禁止越界
        if (src.findParentPosition() != tgt.findParentPosition()) return false

        val msg = if (isGroup) {
            recyclerView.context.getString(
                R.string.group_move_a11y_msg,
                source.modelPosition + 1,
                target.modelPosition + 1
            )
        } else { // subItem
            val from = source.modelPosition - source.findParentPosition()
            val to = tgt.modelPosition - tgt.findParentPosition()
            recyclerView.context.getString(R.string.list_move_a11y_msg, from, to)
        }
        recyclerView.announceForAccessibility(msg)
        return true
    }
}