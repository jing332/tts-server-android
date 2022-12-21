package com.github.jing332.tts_server_android.ui.custom.widget.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.*
import androidx.appcompat.widget.AppCompatAutoCompleteTextView


class MaterialSpinner(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        com.google.android.material.R.attr.autoCompleteTextViewStyle
    )

    init {
        super.setOnItemClickListener { parent, view, position, id ->
            mSelectedPosition = position
            wrappedItemClickListener?.onItemClick(parent, view, position, id)
            super.getOnItemSelectedListener()?.onItemSelected(parent, view, position, id)
        }
        inputType = InputType.TYPE_NULL
        setOnKeyListener(null)
    }

    companion object {
        const val TAG = "MaterialSpinner"
    }


    private var wrappedItemClickListener: AdapterView.OnItemClickListener? = null

    private var mSelectedPosition: Int = 0
        set(value) {
            field = value
            adapter?.selectedItemPosition = value
        }

    /*
        * 当前选中
        * */
    var selectedPosition: Int
        get() = mSelectedPosition
        set(value) {
            mSelectedPosition = value
            updateCurrentPositionText()

            selectedPositionListeners.forEach {
                it.invoke(selectedPosition)
            }
        }

    @SuppressLint("DiscouragedPrivateApi")
    override fun showDropDown() {
        super.showDropDown()
        val mPopupField = AutoCompleteTextView::class.java.getDeclaredField("mPopup")
        mPopupField.isAccessible = true

        val listPopup = mPopupField.get(this) as ListPopupWindow
        listPopup.setSelection(selectedPosition)
    }

    override fun getAdapter(): BaseMaterialSpinnerAdapter<*>? {
        super.getAdapter()?.let {
            return it as BaseMaterialSpinnerAdapter<*>
        }
        return null
    }

    override fun <T> setAdapter(adapter: T?) where T : ListAdapter?, T : Filterable? {
        super.setAdapter(adapter)
        selectedPosition = 0
//        if (adapter is BaseMaterialSpinnerAdapter<*>)
//            addSelectedPositionListener(adapter::selectedItemPosition::set)
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        wrappedItemClickListener = l
    }

    private val selectedPositionListeners: MutableSet<(Int) -> Unit> = mutableSetOf()

/*
    fun addSelectedPositionListener(listener: (Int) -> Unit) {
        selectedPositionListeners.add(listener)
        listener.invoke(selectedPosition)
    }

    fun removeSelectedPositionListener(listener: (Int) -> Unit) {
        selectedPositionListeners.remove(listener)
    }
*/


    private fun updateCurrentPositionText() {
        adapter?.let {
            setText(
                if (it.count > selectedPosition && selectedPosition >= 0) {
                    it.getItem(selectedPosition).toString()
                } else {
                    "无"
                }, false
            )
        }
    }

}