package com.github.jing332.tts_server_android.ui.custom.adapter

import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.textfield.TextInputLayout

class TextInputLayoutDelegate(private val til: TextInputLayout) :
    TextInputLayout.AccessibilityDelegate(til) {
    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        info.text = "${til.hint} ${til.editText?.text}"
    }
}

fun TextInputLayout.initAccessibilityDelegate(){
    setTextInputAccessibilityDelegate(TextInputLayoutDelegate(this))
}
