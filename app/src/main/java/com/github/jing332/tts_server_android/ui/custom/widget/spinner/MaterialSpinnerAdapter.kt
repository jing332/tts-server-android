package com.github.jing332.tts_server_android.ui.custom.widget.spinner

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.DrawableRes
import com.github.jing332.tts_server_android.databinding.ItemMaterialSpinnerBinding

data class SpinnerItem(
    var displayText: String,
    var value: Any? = null,
    @DrawableRes var imageResId: Int = -1
) {
    override fun toString() = displayText
}

class MaterialSpinnerAdapter(var content: Context, var items: List<SpinnerItem>) :
    BaseMaterialSpinnerAdapter<SpinnerItem>(content, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View

        val binding: ItemMaterialSpinnerBinding
        if (convertView == null) { // 新建
            binding = ItemMaterialSpinnerBinding.inflate(layoutInflater, parent, false)
            view = binding.root
            view.tag = binding
        } else { // 复用
            view = convertView
            binding = view.tag as ItemMaterialSpinnerBinding
        }


        val item = getItem(position)
        binding.text = item.displayText
        val isSelected = position == selectedItemPosition

        binding.selected = isSelected
        binding.tv.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
        if (item.imageResId != -1)
            binding.imageView.setImageResource(item.imageResId)

        view.accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.text = binding.text
                info.isCheckable = true
                info.isChecked = binding.selected == true
            }
        }

        return view
    }
}
