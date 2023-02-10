package com.github.jing332.tts_server_android.help.plugin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.AdapterView
import com.github.jing332.tts_server_android.ui.view.AppMaterialSpinner
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem

@SuppressLint("ViewConstructor")
class JSpinner(context: Context, hint: String) : AppMaterialSpinner(context), JViewInterface {
    init {
        super.hint = hint
    }

    override val marginParams: MarginLayoutParams
        get() = layoutParams as MarginLayoutParams

    var items: Map<Any, String> = emptyMap()
        get() = field
        set(value) {
            field = value
            setListModel(value.map { SpinnerItem(it.value, it.key) })
        }


    interface OnItemSelectedListener {
        fun onItemSelected(spinner: JSpinner, position: Int, key: Any?)
    }

    fun setOnItemSelected(listener: OnItemSelectedListener?) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var key: Any? = null
                items.onEachIndexed { index, entry ->
                    if (index == position) {
                        key = entry.key
                    }
                }
                listener?.onItemSelected(this@JSpinner, position, key)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }
}