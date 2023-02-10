package com.github.jing332.tts_server_android.ui.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.R
import com.google.android.material.textfield.TextInputLayout

open class AppTextInputLayout(context: Context, attrs: AttributeSet? = null, defaultStyle: Int = R.attr.textInputStyle) :
    TextInputLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.textInputStyle
    )


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setTextInputAccessibilityDelegate(TextInputLayoutDelegate(this))
    }

    class TextInputLayoutDelegate(private val til: TextInputLayout) : AccessibilityDelegate(til) {
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.text = "${til.hint} ${til.editText?.text}"
        }
    }
}