@file:Suppress("unused")

package com.github.jing332.tts_server_android.model.rhino.core.type.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import com.github.jing332.tts_server_android.compose.widgets.AppSpinner

@SuppressLint("ViewConstructor")
class JSpinner(context: Context, val hint: CharSequence) : AbstractComposeView(context) {

    var items: List<Item> = listOf()

    private var mSelectedPosition by mutableIntStateOf(0)

    var selectedPosition: Int
        get() = mSelectedPosition
        set(value) {
            mSelectedPosition = value
        }

    private var mListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(spinner: JSpinner, position: Int, item: Item)
    }

    fun setOnItemSelected(listener: OnItemSelectedListener?) {
        mListener = listener
    }

    @Composable
    override fun Content() {
        val item = items.getOrNull(mSelectedPosition) ?: return
        AppSpinner(
            label = { Text(hint.toString()) },
            value = item.value,
            values = items.map { it.value },
            entries = items.map { it.name.toString() },
            onSelectedChange = { value, _ ->
                val index = items.indexOfFirst { it.value == value }
                mSelectedPosition = index
                mListener?.onItemSelected(
                    this@JSpinner,
                    index,
                    items[index]
                )
            }
        )
    }


}