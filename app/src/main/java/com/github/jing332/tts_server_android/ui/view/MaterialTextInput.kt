package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.jing332.tts_server_android.databinding.MaterialTextInputBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MaterialTextInput(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    FrameLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    val binding: MaterialTextInputBinding by lazy {
        MaterialTextInputBinding.inflate(LayoutInflater.from(context), this, true)
    }

    val inputEdit: TextInputEditText by lazy { binding.et }
    val inputLayout: TextInputLayout by lazy { binding.til }
}