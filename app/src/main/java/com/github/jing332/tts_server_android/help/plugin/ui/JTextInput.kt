package com.github.jing332.tts_server_android.help.plugin.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput

@SuppressLint("ViewConstructor")
class JTextInput(context: Context, hint: String? = null) : MaterialTextInput(context) {
    init {
        super.setHint(hint)
    }

    interface OnTextChangedListener {
        fun onChanged(text: CharSequence)
    }

    fun addTextChangedListener(listener: OnTextChangedListener) {
        editText!!.addTextChangedListener {
            listener.onChanged(it.toString())
        }
    }

}