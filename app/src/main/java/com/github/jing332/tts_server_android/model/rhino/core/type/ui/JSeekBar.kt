package com.github.jing332.tts_server_android.model.rhino.core.type.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.utils.ThrottleUtil
import com.github.jing332.tts_server_android.utils.toScale

@Suppress("unused")
@SuppressLint("ViewConstructor")
class JSeekBar(context: Context, val hint: CharSequence) : FrameLayout(context) {

    interface OnSeekBarChangeListener {
        fun onStartTrackingTouch(seekBar: JSeekBar)
        fun onProgressChanged(seekBar: JSeekBar, progress: Int, fromUser: Boolean)
        fun onStopTrackingTouch(seekBar: JSeekBar)
    }

    private var mListener: OnSeekBarChangeListener? = null

    fun setOnChangeListener(listener: OnSeekBarChangeListener?) {
        mListener = listener
    }

    init {
        val compose = ComposeView(context)
        addView(compose)
        compose.setContent {
            Content()
        }

    }

    @JvmField
    var max = 0


    private var n = 0
    private var x = 1f
    fun setFloatType(n: Int) {
        this.n = n
        x = 1f
        for (i in 1..n) {
            x *= 10f
        }
    }

    var value: Float
        get() = mValue / x
        set(value) {
            mValue = value * x
            mListener?.onProgressChanged(this@JSeekBar, value.toInt(), false)
        }

    private var mValue by mutableFloatStateOf(0f)
    private val throttleUtils = ThrottleUtil()

    @Composable
    fun Content() {
        LabelSlider(
            value = mValue,
            valueRange = 0f..max.toFloat(),
            buttonSteps = 1f,
            buttonLongSteps = 10f,
            onValueChange = {
                mValue = it
                mListener?.onProgressChanged(this@JSeekBar, value.toInt(), true)

                throttleUtils.runAction {
                    mListener?.onStopTrackingTouch(this@JSeekBar)
                }
            },
            text = hint.toString() + value.toScale(n)
        )
    }

}