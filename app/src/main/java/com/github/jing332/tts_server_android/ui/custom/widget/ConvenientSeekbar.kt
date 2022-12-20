package com.github.jing332.tts_server_android.ui.custom.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.*
import android.view.accessibility.AccessibilityEvent
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
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
        }

    var hint: String
        get() = binding.tvHint.text.toString()
        set(value) {
            binding.tvHint.text = value
        }

    var textValue: String
        get() = binding.tvValue.text.toString()
        set(value) {
            binding.tvValue.contentDescription = "${hint}=${value}"
            binding.tvValue.text = value
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
            progress += 1
            onSeekBarChangeListener?.onStopTrackingTouch(this)
        }
        binding.add.setOnLongClickListener {
            progress += 10
            onSeekBarChangeListener?.onStopTrackingTouch(this)
            true
        }

        binding.remove.setOnClickListener {
            progress -= 1
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
    }

    var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    interface OnSeekBarChangeListener {
        fun onProgressChanged(seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean) {}
        fun onStartTrackingTouch(seekBar: ConvenientSeekbar) {}
        fun onStopTrackingTouch(seekBar: ConvenientSeekbar) {}
    }

    // from https://stackoverflow.com/a/64703579/13197001
    private inner class SeekbarAccessibilityDelegate : AccessibilityDelegateCompat() {
        /**
         * If the selected view is the slider, populate the text to read by talkback.
         * On Android 10 and 9, the view got selected from the beginning without touch the slider,
         * so the TYPE_VIEW_SELECTED event is controlled.
         * The text should be overwritten to trigger what Talkback do need to read.
         *
         * @param host The view selected
         * @param event The event to initialise
         */
        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            if (event.eventType != AccessibilityEvent.TYPE_VIEW_SELECTED) {
                textValue = textValue
            }
        }

        /**
         * Send all accessibility events except the focused accessibility event
         * because it reads the percentage, so it needs to be changed to no focused to read
         * the sliderText.
         *
         * @param host the view selected
         * @param eventType The accessibility event to send
         */
        override fun sendAccessibilityEvent(host: View, eventType: Int) {
            var type = eventType
            if (eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                type = AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED
            }
            super.sendAccessibilityEvent(host, type)
        }

        /**
         * If the slider changes, it won't send the AccessibilityEvent TYPE_WINDOW_CONTENT_CHANGED
         * because it reads the percentages, so in that way it will read the sliderText.
         * On Android 10 and 9, the view got selected when it changes, so the TYPE_VIEW_SELECTED
         * event is controlled.
         *
         * @param host the view selected
         * @param event the accessibility event to send
         */
        override fun sendAccessibilityEventUnchecked(host: View, event: AccessibilityEvent) {
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && event.eventType != AccessibilityEvent.TYPE_VIEW_SELECTED
            ) {
                super.sendAccessibilityEventUnchecked(host, event)
            }
        }
    }
}