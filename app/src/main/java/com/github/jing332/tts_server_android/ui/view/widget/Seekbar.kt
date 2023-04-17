package com.github.jing332.tts_server_android.ui.view.widget

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
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar.ValueFormatter
import com.github.jing332.tts_server_android.utils.ThrottleUtil
import kotlin.math.roundToInt

open class Seekbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : ConstraintLayout(context, attrs, defaultStyle), OnSeekBarChangeListener {

    private val binding by lazy {
        SeekbarBinding.inflate(LayoutInflater.from(context), this, true)
    }


    var max: Int = 100
        set(value) {
            field = value

            binding.seekBar.max = max - min
        }

    var min: Int = 0
        set(value) {
            field = value
            if (min < 0) // 负数 实现偏移
                progressConverter = object : ProgressConverter {
                    override fun valueToProgress(value: Any): Int {
                        return (value as Int) - min
                    }

                    override fun progressToValue(progress: Int): Any {
                        return progress + min
                    }
                }
        }


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
        max = ta.getInteger(R.styleable.Seekbar_max, max)
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

            // 禁用原生的百分比朗读
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
        ) {
        }

        fun onStartTrackingTouch(seekBar: Seekbar) {}
        fun onStopTrackingTouch(seekBar: Seekbar) {}
    }

    companion object {
        private val defaultValueFormatter = ValueFormatter { value, _ ->
            value.toString()
        }

        private val defaultProgressConverter = object : ProgressConverter {
            override fun valueToProgress(value: Any): Int = value as Int
            override fun progressToValue(progress: Int): Any = progress
        }

        private val a11yThrottle: ThrottleUtil by lazy { ThrottleUtil(time = 250) }
    }

    /**
     * 设为小数显示, n为小数点后几位
     * @param n 必须是 1 or 2
     */
    fun setFloatType(n: Int) {
        assert(n in 1..2)
        var x = 1f
        for (i in 1..n) {
            x *= 10
        }

        progressConverter = object : ProgressConverter {
            override fun valueToProgress(value: Any): Int {
                return ((value as Float) * x).roundToInt() - min
            }

            override fun progressToValue(progress: Int): Any {
                return (progress + min) / x
            }

        }
    }

    /*
    * Seekbar进度与实际Value的转换器
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
         * 自定义显示Value的格式化
         */
        fun getFormattedValue(value: Any, progress: Int): String
    }

    // 防止 progress 相同时不回调 onProgressChanged
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