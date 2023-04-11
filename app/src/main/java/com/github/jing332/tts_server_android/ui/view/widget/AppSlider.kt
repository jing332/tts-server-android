package com.github.jing332.tts_server_android.ui.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SliderBinding
import com.google.android.material.slider.Slider
import splitties.systemservices.layoutInflater
import java.util.Locale




@SuppressLint("PrivateResource", "CustomViewStyleable")
class AppSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : ConstraintLayout(context, attrs, defaultStyle) {
    private val binding by lazy {
        SliderBinding.inflate(layoutInflater, this, true)
    }

    val slider: Slider
        get() = binding.slider

    var value: Float
        get() = binding.slider.value
        set(value) {
            binding.slider.value = value
        }

    var valueFrom: Float
        get() = binding.slider.valueFrom
        set(value) {
            binding.slider.valueFrom = value
        }

    var valueTo: Float
        get() = binding.slider.valueTo
        set(value) {
            binding.slider.valueTo = value
        }

    var stepSize: Float
        get() = binding.slider.stepSize
        set(value) {
            binding.slider.stepSize = value
        }

    var hint: CharSequence
        get() = binding.tvHint.text
        set(value) {
            binding.tvHint.text = value
        }

    init {
        slider.addOnChangeListener { slider, value, fromUser ->
            binding.tvValue.text = value.toString()
//            if (fromUser) {
//                slider.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
//            }
        }

        binding.add.setOnClickListener {
            if (value < valueTo)
                value += stepSize
        }
        binding.add.setOnLongClickListener {
            if (value + 10 < valueTo)
                value += stepSize * 10
            true
        }

        binding.remove.setOnClickListener {
            if (value > valueFrom)
                value -= stepSize

        }
        binding.remove.setOnLongClickListener {
            if (valueTo > value + 10)
                value -= stepSize
            true
        }

        val ta =
            context.obtainStyledAttributes(attrs, com.google.android.material.R.styleable.Slider)
        value = 1f
        valueFrom =
            ta.getFloat(com.google.android.material.R.styleable.Slider_android_valueFrom, 1f)
        valueTo = ta.getFloat(com.google.android.material.R.styleable.Slider_android_valueTo, 100f)
        stepSize = ta.getFloat(com.google.android.material.R.styleable.Slider_android_stepSize, 1f)
        value = ta.getFloat(com.google.android.material.R.styleable.Slider_android_value, valueFrom)

        binding.tvHint.text = ta.getString(R.styleable.AppSlider_hint)
        ta.recycle()

        binding.remove.contentDescription = "$hint -1"
        binding.add.contentDescription = "$hint +1"

        slider.setLabelFormatter {
             String.format(Locale.getDefault(), "%.2f", value.toFloat() / stepSize.toFloat())
        }

        binding.slider.accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_ASSERTIVE
        ViewCompat.setAccessibilityDelegate(binding.slider, object :
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


}
