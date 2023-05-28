package com.github.jing332.tts_server_android.model.rhino.core.type.ui

import android.annotation.SuppressLint
import android.content.Context
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

@SuppressLint("ViewConstructor")
class JSeekBar(context: Context, hint: CharSequence) : Seekbar(context), Seekbar.OnSeekBarChangeListener {
    init {
        super.hint = hint.toString()
    }

    fun setOnChangeListener(listener: OnSeekBarChangeListener?) {
        super.onSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: Seekbar) {
                kotlin.runCatching {
                    listener?.onStartTrackingTouch(seekBar)
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }

            override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
                kotlin.runCatching {
                    listener?.onProgressChanged(seekBar, progress, fromUser)
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }

            override fun onStopTrackingTouch(seekBar: Seekbar) {
                kotlin.runCatching {
                    listener?.onStopTrackingTouch(seekBar)
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }
        }


    }
}