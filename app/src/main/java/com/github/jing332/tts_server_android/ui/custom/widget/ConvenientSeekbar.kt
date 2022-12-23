package com.github.jing332.tts_server_android.ui.custom.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.*
import android.view.accessibility.AccessibilityEvent
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.postDelayed
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ConvenientSeekbarBinding


@SuppressLint("ClickableViewAccessibility")
class ConvenientSeekbar(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding by lazy {
        ConvenientSeekbarBinding.inflate(LayoutInflater.from(context), this, true)
    }
    val seekBar by lazy { binding.seekBar }

    /* var max: Int
         inline get() = seekBar.max
         inline set(value) {
             seekBar.max = value
         }*/
    var progress: Int
        inline get() = seekBar.progress
        inline set(value) {
            seekBar.progress = value
            onSeekBarChangeListener?.onProgressChanged(this, value, false)
        }

    var hint: String
        get() = binding.tvHint.text.toString()
        set(value) {
            binding.tvHint.text = value
        }

    private val mA11yHandler by lazy { Handler(Looper.getMainLooper()) }

    var textValue: String
        get() = binding.tvValue.text.toString()
        set(value) {
            binding.apply {
                tvValue.text = value

                // 防高频调用
                mA11yHandler.removeCallbacksAndMessages(null)
                mA11yHandler.postDelayed(150) {
                    if (seekBar.isAccessibilityFocused || remove.isAccessibilityFocused || add.isAccessibilityFocused)
                        seekBar.announceForAccessibility("$hint $value")
                }
            }
        }

    init {
        ViewCompat.setAccessibilityDelegate(binding.seekBar, SeekbarAccessibilityDelegate())
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onSeekBarChangeListener?.onProgressChanged(
                    this@ConvenientSeekbar,
                    progress,
                    fromUser
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                onSeekBarChangeListener?.onStartTrackingTouch(this@ConvenientSeekbar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onSeekBarChangeListener?.onStopTrackingTouch(this@ConvenientSeekbar)
            }
        })

        binding.add.setOnClickListener {
            progress++
            onSeekBarChangeListener?.onStopTrackingTouch(this)
        }
        binding.add.setOnLongClickListener {
            progress += 10
            onSeekBarChangeListener?.onStopTrackingTouch(this)
            true
        }

        binding.remove.setOnClickListener {
            progress--
            onSeekBarChangeListener?.onStopTrackingTouch(this)
        }
        binding.remove.setOnLongClickListener {
            progress -= 10
            onSeekBarChangeListener?.onStopTrackingTouch(this)
            true
        }

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ConvenientSeekbar)
        seekBar.progress = ta.getInteger(R.styleable.ConvenientSeekbar_progress, 0)
        seekBar.max = ta.getInteger(R.styleable.ConvenientSeekbar_max, 100)
        binding.tvHint.text = ta.getString(R.styleable.ConvenientSeekbar_hint)
        ta.recycle()

        binding.remove.contentDescription = "$hint -1"
        binding.add.contentDescription = "$hint +1"
    }

    var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    interface OnSeekBarChangeListener {
        fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {}
        fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}
        fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {}
    }

    // https://stackoverflow.com/a/64703579/13197001
    private inner class SeekbarAccessibilityDelegate : AccessibilityDelegateCompat() {
        // 单击Seekbar时调用
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.text = "$hint $textValue"
            textValue = textValue
        }

        // 禁用原生的百分比朗读
        override fun sendAccessibilityEventUnchecked(host: View, event: AccessibilityEvent) {
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && event.eventType != AccessibilityEvent.TYPE_VIEW_SELECTED
            ) {
                super.sendAccessibilityEventUnchecked(host, event)
            }
        }
    }
}