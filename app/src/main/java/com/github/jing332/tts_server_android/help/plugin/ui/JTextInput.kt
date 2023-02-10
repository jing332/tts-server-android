package com.github.jing332.tts_server_android.help.plugin.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput

@SuppressLint("ViewConstructor")
class JTextInput(context: Context, hint: String? = null) : MaterialTextInput(context),
    JViewInterface {
    override val marginParams: MarginLayoutParams
        get() = layoutParams as MarginLayoutParams

    var hint: CharSequence?
        get() = editLayout.hint
        set(value) {
            editLayout.hint = value
        }

    var text: CharSequence?
        get() = editText.text
        set(value) {
            editText.setText(value)
        }

    interface OnTextChangedListener {
        fun onChanged(text: CharSequence)
    }

    fun addTextChangedListener(listener: OnTextChangedListener) {
        editText.addTextChangedListener {
            listener.onChanged(it.toString())
        }
    }

    init {
        super.editLayout.hint = hint
    }
}