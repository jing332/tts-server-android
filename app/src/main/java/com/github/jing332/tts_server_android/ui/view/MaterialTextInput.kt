package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import com.github.jing332.tts_server_android.ui.view.widget.AppTextInputLayout
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

open class MaterialTextInput(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    AppTextInputLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, R.attr.textInputStyle)

    var text: Editable
        get() = editText!!.text
        set(value) {
            editText!!.text = value
        }

    init {
        @Suppress("LeakingThis")
        addView(TextInputEditText(getContext()))
    }
}