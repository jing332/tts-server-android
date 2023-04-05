package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.github.jing332.tts_server_android.databinding.SysttsBgmParamsEditViewBinding
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.util.inflateBinding
import com.github.jing332.tts_server_android.util.layoutInflater

abstract class BaseParamsEditView<VB : ViewBinding, T : ITextToSpeechEngine> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : ConstraintLayout(context, attrs, defaultStyle) {

    protected val binding: VB by lazy { inflateBinding(context.layoutInflater, this, true) }
    protected open var tts: T? = null

    open fun setData(tts: T) {
        this.tts = tts
    }
}
