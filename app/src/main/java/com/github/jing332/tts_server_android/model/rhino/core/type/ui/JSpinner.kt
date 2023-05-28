@file:Suppress("unused")

package com.github.jing332.tts_server_android.model.rhino.core.type.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.AdapterView
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.AppMaterialSpinner
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem

@SuppressLint("ViewConstructor")
class JSpinner(context: Context, hint: CharSequence) : AppMaterialSpinner(context) {
    init {
        super.hint = hint
    }

    var items: List<Item> = listOf()
        set(value) {
            field = value
            setListModel(value.map { SpinnerItem(it.name.toString(), it.value) })
        }

    interface OnItemSelectedListener {
        fun onItemSelected(spinner: JSpinner, position: Int, item: Item)
    }

    fun setOnItemSelected(listener: OnItemSelectedListener?) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                kotlin.runCatching {
                    listener?.onItemSelected(this@JSpinner, position, items[position])
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }
}