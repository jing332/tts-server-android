package com.github.jing332.tts_server_android.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.jing332.tts_server_android.databinding.MaterialSpinnerBinding
import com.github.jing332.tts_server_android.ui.view.widget.AppTextInputLayout
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinner
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem

open class WrapperMaterialSpinner(context: Context) : FrameLayout(context) {
    private val binding by lazy {
        MaterialSpinnerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    var selectedPosition: Int
        get() = spinner.selectedPosition
        set(value) {
            spinner.selectedPosition = value
        }

    val til: AppTextInputLayout
        get() = binding.til

    val spinner: MaterialSpinner
        get() = binding.spinner

    fun setListModel(list: List<SpinnerItem>) {
        spinner.setAdapter(MaterialSpinnerAdapter(context, list))
    }

}
