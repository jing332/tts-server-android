package com.github.jing332.tts_server_android.ui.view.widget.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.MaterialSpinnerDialogBinding
import com.github.jing332.tts_server_android.databinding.MaterialSpinnerDialogItemBinding
import com.github.jing332.tts_server_android.ui.view.Attributes.colorAttr
import com.github.jing332.tts_server_android.ui.view.Attributes.selectableItemBackground
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import splitties.systemservices.layoutInflater


@SuppressLint("DiscouragedPrivateApi")
class MaterialSpinner(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    MaterialAutoCompleteTextView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        com.google.android.material.R.attr.autoCompleteTextViewStyle
    )

    companion object {
        const val TAG = "MaterialSpinner"

        val accessibilityManager by lazy {
            App.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        }
    }

    init {
        super.setOnItemClickListener { parent, view, position, id ->
            mSelectedPosition = position
            wrappedItemClickListener?.onItemClick(parent, view, position, id)
            super.getOnItemSelectedListener()?.onItemSelected(parent, view, position, id)
        }
        inputType = InputType.TYPE_NULL
    }

    private var wrappedItemClickListener: AdapterView.OnItemClickListener? = null

    private var mSelectedPosition: Int = 0
        set(value) {
            field = value
            adapter?.selectedItemPosition = value
        }

    var selectedPosition: Int
        get() = mSelectedPosition
        set(value) {
            mSelectedPosition = value
            updateCurrentPositionText()
        }

    private val listDialog by lazy {
        MaterialAlertDialogBuilder(context).setPositiveButton(
            R.string.cancel,
            null
        )
    }

    override fun showDropDown() {
        requestFocus()
        val items = adapter?.items?.map { it as SpinnerItem } ?: emptyList()
        if (accessibilityManager.isTouchExplorationEnabled || items.size > 20) {
            displayChooseDialog(items)
        } else {
            super.showDropDown()
            listSelection = selectedPosition

        }
    }


    private fun displayChooseDialog(items: List<SpinnerItem>) {
        val dlgBinding = MaterialSpinnerDialogBinding.inflate(layoutInflater)
        val dlg = MaterialAlertDialogBuilder(context)
            .setTitle(hint)
            .apply {
                if (items.isEmpty()) setMessage(R.string.empty_list)
            }
            .setView(dlgBinding.root)
            .setOnDismissListener {
                updateCurrentPositionText()
            }
            .setPositiveButton(R.string.cancel, null)
            .show()

        dlgBinding.apply {
            FastScrollerBuilder(rv).build()

            val brv = rv.linear().setup {
                addType<DialogItemModel>(R.layout.material_spinner_dialog_item)
                onCreate {
                    getBinding<MaterialSpinnerDialogItemBinding>().apply {
                        card.clickWithThrottle {
                            val model = getModel<DialogItemModel>()
                            selectedPosition = model.position
                            super.getOnItemClickListener()
                                ?.onItemClick(null, it, selectedPosition, -1)
                            dlg.dismiss()
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
                    getBinding<MaterialSpinnerDialogItemBinding>().apply {
                        val model = getModel<DialogItemModel>()
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

            val fullModels = items.mapIndexed { index, item ->
                DialogItemModel(
                    text = item.displayText,
                    isChecked = index == selectedPosition,
                    position = index
                )
            }

            brv.models = fullModels
            rv.scrollToPosition(selectedPosition)

            tilFilter.editText?.addTextChangedListener { editable ->
                brv.models =
                    fullModels.filter { it.text.contains(editable.toString().trim(), true) }
            }
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

    // 供DataBindingAdapter使用
    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        wrappedItemClickListener = l
    }

    fun callItemSelected(pos: Int = mSelectedPosition) {
        onItemSelectedListener?.onItemSelected(null, null, pos, 0)
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