package com.github.jing332.tts_server_android.help.plugin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar

@SuppressLint("ViewConstructor")
class JSeekBar(context: Context, hint: String) : Seekbar(context), Seekbar.OnSeekBarChangeListener {
    init {
        super.hint = hint
    }

    fun setOnChangeListener(listener: OnSeekBarChangeListener?) {
        super.onSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: Seekbar) {
                kotlin.runCatching {
                    listener?.onStartTrackingTouch(seekBar)
                }.onFailure {
                    AppDialogs.displayErrorDialog(context, it.stackTraceToString())
                }
            }

            override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
                kotlin.runCatching {
                    listener?.onProgressChanged(seekBar, progress, fromUser)
                }.onFailure {
                    AppDialogs.displayErrorDialog(context, it.stackTraceToString())
                }
            }

            override fun onStopTrackingTouch(seekBar: Seekbar) {
                kotlin.runCatching {
                    listener?.onStopTrackingTouch(seekBar)
                }.onFailure {
                    AppDialogs.displayErrorDialog(context, it.stackTraceToString())
                }
            }
        }


    }
}