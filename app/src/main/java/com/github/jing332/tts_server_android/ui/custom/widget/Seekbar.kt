package com.github.jing332.tts_server_android.ui.custom.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SeekbarBinding
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar.ValueFormatter
import com.github.jing332.tts_server_android.util.ThrottleUtil

class Seekbar(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding by lazy {
        SeekbarBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var min: Int = 0

    var progress: Int
        get() = binding.seekBar.progress
        set(value) {
            setSeekbarProgress(value)
        }

    var hint: String
        get() = binding.tvHint.text.toString()
        set(value) {
            binding.tvHint.text = value
        }

    init {
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

        val ta = context.obtainStyledAttributes(attrs, R.styleable.Seekbar)
        binding.seekBar.progress = ta.getInteger(R.styleable.Seekbar_progress, 0)
        min = ta.getInteger(R.styleable.Seekbar_min, 0)
        binding.seekBar.max = ta.getInteger(R.styleable.Seekbar_max, 100)
        binding.tvHint.text = ta.getString(R.styleable.Seekbar_hint)
        ta.recycle()

        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.remove.contentDescription = "$hint -1"
        binding.add.contentDescription = "$hint +1"

        binding.seekBar.accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_ASSERTIVE
        ViewCompat.setAccessibilityDelegate(binding.seekBar, object :
            AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.text = "$hint ${binding.tvValue.text}"
            }

            // ??????????????????????????????
            override fun sendAccessibilityEventUnchecked(
                host: View,
                event: AccessibilityEvent
            ) {
                if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                    && event.eventType != AccessibilityEvent.TYPE_VIEW_SELECTED
                ) {
                    super.sendAccessibilityEventUnchecked(host, event)
                }
            }
        })
    }

    var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    interface OnSeekBarChangeListener {
        fun onProgressChanged(
            seekBar: Seekbar,
            progress: Int,
            fromUser: Boolean
        )

        fun onStartTrackingTouch(seekBar: Seekbar) {}
        fun onStopTrackingTouch(seekBar: Seekbar) {}
    }

    companion object {
        private val defaultValueFormatter = ValueFormatter { value, progress ->
            value.toString()
        }

        private val defaultProgressConverter = object : ProgressConverter {
            override fun valueToProgress(value: Any): Int = value as Int
            override fun progressToValue(progress: Int): Any = progress
        }

        private val a11yThrottle: ThrottleUtil by lazy { ThrottleUtil(time = 250) }
    }

    /*
    * Seekbar???????????????Value????????????
    * */
    var progressConverter: ProgressConverter? = null

    private val mProgressConverter: ProgressConverter
        get() = progressConverter ?: defaultProgressConverter

    interface ProgressConverter {
        fun valueToProgress(value: Any): Int
        fun progressToValue(progress: Int): Any
    }

    var value: Any = 0
        get() = mProgressConverter.progressToValue(progress)
        set(value) {
            field = value
            setSeekbarProgress(mProgressConverter.valueToProgress(value))
        }

    var valueFormatter: ValueFormatter? = null

    private val mValueFormatter: ValueFormatter
        get() = valueFormatter ?: defaultValueFormatter

    fun interface ValueFormatter {
        /**
         * ???????????????Value????????????
         */
        fun getFormattedValue(value: Any, progress: Int): String
    }

    // ?????? progress ?????????????????? onProgressChanged
    private fun setSeekbarProgress(progress: Int) {
        if (progress == binding.seekBar.progress)
            onProgressChanged(binding.seekBar, progress, false)
        else binding.seekBar.progress = progress
    }

    private val accessibilityManager by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    override fun onProgressChanged(seekBar: SeekBar?, _progress: Int, fromUser: Boolean) {
        var progress = _progress
        if (progress < min) {
            setSeekbarProgress(min)
            progress = min
        }

        val value = mValueFormatter.getFormattedValue(
            mProgressConverter.progressToValue(progress), progress
        )
        binding.tvValue.text = value
        if (accessibilityManager.isTouchExplorationEnabled)
            a11yThrottle.runAction {
                onStopTrackingTouch(seekBar)
                if (binding.seekBar.isAccessibilityFocused || binding.add.isAccessibilityFocused || binding.remove.isAccessibilityFocused)
                    seekBar?.announceForAccessibility("$hint $value")
            }

        onSeekBarChangeListener?.onProgressChanged(this, progress, fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        onSeekBarChangeListener?.onStartTrackingTouch(this)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        onSeekBarChangeListener?.onStopTrackingTouch(this)
    }

}