package com.github.jing332.tts_server_android.help.plugin.ui

import android.annotation.SuppressLint
import android.content.Context
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

@SuppressLint("ViewConstructor")
class JSeekBar(context: Context, hint:String) : Seekbar(context),JViewInterface {
    override val marginParams: MarginLayoutParams
        get() = layoutParams as MarginLayoutParams

    init {
        super.hint = hint
    }
}