package com.github.jing332.tts_server_android.ui.systts.edit

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.github.jing332.tts_server_android.BR
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem

/**
 * MVVM ViewBinding 直接在ViewModel中操作Spinner下拉框数据
 */
class SpinnerData : BaseObservable() {
    // 包装成lambda
    fun addOnPropertyChangedCallback(callback: (sender: Observable?, propertyId: Int) -> Unit) {
        super.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                callback(sender, propertyId)
            }
        })
    }

    fun reset() {
        items = listOf()
        position = 0
    }

    // View -> ViewModel 单向绑定
    @Bindable("items")
    var items: List<SpinnerItem> = emptyList()
        set(value) {
            field = value
            notifyPropertyChanged(BR.items)
        }

    // 双向绑定
    @Bindable("position")
    var position: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.position)
        }

    val selectedItem: SpinnerItem?
        get() {
            return items.getOrNull(position)
        }
}