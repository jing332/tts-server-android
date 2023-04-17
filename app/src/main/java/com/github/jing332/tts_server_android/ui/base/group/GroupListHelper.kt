package com.github.jing332.tts_server_android.ui.base.group

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import com.drake.brv.BindingAdapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.BaseListGroupItemBinding
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GroupListHelper<T : IGroupModel>(val context: Context) {
    interface Callback<T : IGroupModel> {
        fun onGroupClick(v: View, model: T)
        fun onCheckedChange(v: MaterialCheckBox, model: T)
        fun onExport(v: View, model: T)
        fun onRemove(v: View, model: T)
        fun onRename(v: View, model: T)
    }

    var callback: Callback<T>? = null

    /**
     * addType<IGroupModel>(R.layout.base_list_group_item)
     */
    fun initGroup(
        adapter: BindingAdapter,
        holder: BindingAdapter.BindingViewHolder,
    ) {
        context.apply {
            // setup()
            adapter.apply {

                // onCreate()
                holder.apply {
                    // 分组条目
                    getBindingOrNull<BaseListGroupItemBinding>()?.apply {
                        itemView.setOnLongClickListener {
                            itemTouchHelper?.startDrag(holder)
                            true
                        }

                        itemView.accessibilityDelegate = object : View.AccessibilityDelegate() {
                            override fun onInitializeAccessibilityNodeInfo(
                                host: View,
                                info: AccessibilityNodeInfo
                            ) {
                                super.onInitializeAccessibilityNodeInfo(host, info)
                                val model = getModel<IGroupModel>()
                                info.text =
                                    "${ivState.contentDescription}, ${tvName.text}, ${
                                        getString(
                                            R.string.systts_desc_list_count_info,
                                            model.itemSublist?.size,
                                            model.subItemEnabledCount
                                        )
                                    }"
                            }
                        }

                        itemView.clickWithThrottle {
                            val model = getModel<T>()
                            val isExpanded = !model.itemExpand

                            val speakText =
                                if (isExpanded) R.string.group_expanded else R.string.group_collapsed
                            itemView.announceForAccessibility(
                                getString(speakText) + ", ${
                                    getString(
                                        R.string.systts_desc_list_count_info,
                                        model.itemSublist?.size,
                                        model.subItemEnabledCount
                                    )
                                }"
                            )

                            if (model.itemSublist.isNullOrEmpty()) {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.msg_group_is_empty)
                                    .setMessage(getString(R.string.systts_group_empty_msg))
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                            }

                            callback?.onGroupClick(it, model)
                        }

                        checkBox.accessibilityDelegate = object : View.AccessibilityDelegate() {
                            override fun onInitializeAccessibilityNodeInfo(
                                host: View,
                                info: AccessibilityNodeInfo
                            ) {
                                super.onInitializeAccessibilityNodeInfo(host, info)
                                val str = when (checkBox.checkedState) {
                                    MaterialCheckBox.STATE_CHECKED -> getString(R.string.md_checkbox_checked)
                                    MaterialCheckBox.STATE_UNCHECKED -> getString(R.string.md_checkbox_unchecked)
                                    MaterialCheckBox.STATE_INDETERMINATE -> getString(R.string.md_checkbox_indeterminate)
                                    else -> ""
                                }
                                info.text = ", $str, "
                            }
                        }
                        checkBox.clickWithThrottle {
                            callback?.onCheckedChange(checkBox, getModel())
                        }
                        btnMore.clickWithThrottle {
                            PopupMenu(context, it).apply {
                                this.setForceShowIcon(true)
                                MenuCompat.setGroupDividerEnabled(menu, true)
                                menuInflater.inflate(R.menu.systts_list_group_more, menu)
                                setOnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.menu_export_config ->
                                            callback?.onExport(it, getModel())

                                        R.id.menu_rename_group -> callback?.onRename(it, getModel())
                                        R.id.menu_remove_group -> callback?.onRemove(it, getModel())
                                    }
                                    true
                                }
                                show()
                            }

                        }
                    }


                }
            }
        }
    }
}