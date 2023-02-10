package com.github.jing332.tts_server_android.help.plugin.ui

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.github.jing332.tts_server_android.ui.view.AppMaterialSpinner
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem

class Spinner(context: Context) : AppMaterialSpinner(context) {
    fun setList(map: Map<Any, String>) {
        setListModel(map.map { SpinnerItem(it.value, it.key) })
    }

    interface OnItemSelectedListener {
        fun onItemSelected(spinner: Spinner, position: Int)
    }

    fun setOnItemSelected(listener: OnItemSelectedListener?) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener?.onItemSelected(this@Spinner, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }
}