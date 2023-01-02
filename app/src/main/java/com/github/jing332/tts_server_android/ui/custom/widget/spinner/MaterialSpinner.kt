package com.github.jing332.tts_server_android.ui.custom.widget.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.accessibility.AccessibilityManager
import android.widget.*
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class MaterialSpinner(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    MaterialAutoCompleteTextView(context, attrs, defStyleAttr) {
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
        }

    private val listDialog by lazy { MaterialAlertDialogBuilder(context) }
    private val accessibilityManager by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun showDropDown() {
        if (accessibilityManager.isTouchExplorationEnabled) {
            val adapter =
                ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice)

            val items = getAdapter()!!.items
            adapter.addAll(items.map { (it as SpinnerItem).displayText })
            listDialog.setTitle(context.getString(R.string.choice_item, hint))
            listDialog.setSingleChoiceItems(adapter, selectedPosition) { dlg, which ->
                selectedPosition = which
                super.getOnItemClickListener()?.onItemClick(null, this, which, -1)
                dlg.dismiss()
            }
            listDialog.setFadeAnim().show()
        } else{
            val modalListPopup =
                MaterialAutoCompleteTextView::class.java.getDeclaredField("modalListPopup")
                    .apply { isAccessible = true }
                    .get(this) as androidx.appcompat.widget.ListPopupWindow
            modalListPopup.show()
            modalListPopup.setSelection(selectedPosition)
        }
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
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        wrappedItemClickListener = l
    }

    private fun updateCurrentPositionText() {
        adapter?.let {
            setText(
                if (it.count > selectedPosition && selectedPosition >= 0) {
                    it.getItem(selectedPosition).toString()
                } else {
                    context.getString(R.string.none)
                }, false
            )
        }
    }
}