package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.github.jing332.tts_server_android.databinding.BigTextViewBinding
import splitties.systemservices.layoutInflater

class BigTextView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    FrameLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val mBinding by lazy {
        BigTextViewBinding.inflate(layoutInflater, this, true).apply {
            tvLog.setTextIsSelectable(true)
        }
    }

    fun setText(text: CharSequence) {
        mBinding.tvLog.text = text
    }

    fun append(text: CharSequence) {
        mBinding.tvLog.append(text)
    }
}