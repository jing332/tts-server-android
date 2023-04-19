package com.github.jing332.tts_server_android.ui.view.widget.spinner

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.github.jing332.tts_server_android.databinding.MaterialSpinnerItemBinding

data class SpinnerItem(
    var displayText: String,
    var value: Any? = null,
    @DrawableRes var imageResId: Int = -1
) {
    override fun toString() = displayText
}

class MaterialSpinnerAdapter(val content: Context, items: List<SpinnerItem>) :
    BaseMaterialSpinnerAdapter<SpinnerItem>(content, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View

        val binding: MaterialSpinnerItemBinding
        if (convertView == null) { // 新建
            binding = MaterialSpinnerItemBinding.inflate(layoutInflater, parent, false)
            view = binding.root
            view.tag = binding
        } else { // 复用
            view = convertView
            binding = view.tag as MaterialSpinnerItemBinding
        }

        val item = getItem(position)
        val isChecked = position == selectedItemPosition

        binding.text1.isChecked = isChecked
        binding.text1.text = item.displayText
        binding.text1.setTypeface(null, if (isChecked) Typeface.BOLD else Typeface.NORMAL)

        return view
    }
}
