package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ListChooseItemBinding
import com.github.jing332.tts_server_android.databinding.ListChooseViewBinding
import com.github.jing332.tts_server_android.ui.view.Attributes.colorAttr
import com.github.jing332.tts_server_android.ui.view.Attributes.selectableItemBackground
import com.github.jing332.tts_server_android.ui.view.widget.spinner.DialogItemModel
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import splitties.systemservices.layoutInflater

class ListChooseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding by lazy { ListChooseViewBinding.inflate(layoutInflater, this, true) }

    var onItemClickListener: AdapterView.OnItemClickListener? = null

    /**
     * 是否显示搜索框
     */
    var isSearchFilterEnabled: Boolean
        get() = binding.tilFilter.isVisible
        set(value) {
            binding.tilFilter.isVisible = value
        }

    private var brv: BindingAdapter
    private var fullModels: List<DialogItemModel> = emptyList()

    init {
        binding.apply {
            FastScrollerBuilder(rv).build()

            brv = rv.linear().setup {
                addType<DialogItemModel>(R.layout.list_choose_item)
                onCreate {
                    getBinding<ListChooseItemBinding>().apply {
                        card.clickWithThrottle {
                            val model = getModel<DialogItemModel>()
                            onItemClickListener?.onItemClick(null, it, model.position, -1)
                        }

                        card.accessibilityDelegate = object : AccessibilityDelegate() {
                            override fun onInitializeAccessibilityNodeInfo(
                                host: View,
                                info: AccessibilityNodeInfo
                            ) {
                                super.onInitializeAccessibilityNodeInfo(host, info)
                                info.isClickable = true
                                info.isCheckable = true
                                info.isChecked = getModel<DialogItemModel>().isChecked
                            }
                        }
                    }
                }

                val colorHighLight =
                    context.colorAttr(com.google.android.material.R.attr.colorControlHighlight)
                onBind {
                    getBinding<ListChooseItemBinding>().apply {
                        val model = getModel<DialogItemModel>()
                        text1.text = model.text
                        text1.setTypeface(
                            null, if (model.isChecked) Typeface.BOLD else Typeface.NORMAL
                        )

                        if (model.isChecked)
                            layout.setBackgroundColor(colorHighLight)
                        else
                            layout.setBackgroundResource(context.selectableItemBackground)
                    }
                }
            }

            tilFilter.editText?.addTextChangedListener { editable ->
                brv.models =
                    fullModels.filter { it.text.contains(editable.toString().trim(), true) }
            }
        }
    }

    /***
     * 设置列表数据
     */
    fun setItems(items: List<String>, selectedPosition: Int) {
        fullModels = items.mapIndexed { index, item ->
            DialogItemModel(
                text = item,
                isChecked = index == selectedPosition,
                position = index
            )
        }
        brv.models = fullModels
        binding.rv.scrollToPosition(selectedPosition)
    }

}