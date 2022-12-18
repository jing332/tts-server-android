package com.github.jing332.tts_server_android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.github.jing332.tts_server_android.databinding.MaterialTextInputBinding
import com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MaterialTextInput(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    TextInputLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, hint: String = "") : this(
        context,
        null,
        Widget_Material3_TextInputLayout_OutlinedBox
    ) {
        this.hint = hint
    }

    val binding: MaterialTextInputBinding by lazy {
        MaterialTextInputBinding.inflate(LayoutInflater.from(context), this, true)
    }

    val editView: TextInputEditText by lazy { binding.et }
}