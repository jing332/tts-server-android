package com.github.jing332.tts_server_android.ui.view.widget.spinner

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

@BindingAdapter("items")
fun bindItems(view: MaterialSpinner, items: List<SpinnerItem>?) {
    if (items == null) return
    view.setAdapter(MaterialSpinnerAdapter(view.context, items))
}

@BindingAdapter("selectedPosition")
fun setSelectedPosition(view: MaterialSpinner, position: Int) {
    if (view.selectedPosition != position) view.selectedPosition = position
}

@InverseBindingAdapter(attribute = "selectedPosition")
fun getSelectedPosition(view: MaterialSpinner): Int {
    return view.selectedPosition
}

@BindingAdapter("selectedPositionAttrChanged")
fun listenToSelectedPosition(view: MaterialSpinner, listener: InverseBindingListener) {
    view.setOnItemClickListener { _, _, _, _ ->
        listener.onChange()
    }
}
