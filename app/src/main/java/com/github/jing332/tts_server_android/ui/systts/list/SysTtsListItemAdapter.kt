/*
package com.github.jing332.tts_server_android.ui.systts.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.View
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListItem2Binding
import com.github.jing332.tts_server_android.databinding.SysttsListItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.util.clone
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.setFadeAnim
import kotlin.system.measureTimeMillis



@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun RecyclerView.setupSystemTtsList(
    onSwitchChanged: (v: View, list: List<SysTts>, position: Int) -> Unit,
    onEdit: (tts: SysTts) -> Unit,
    onDelete: (tts: SysTts) -> Unit,
    onItemClick: (tts: SysTts) -> Unit,
    onItemLongClick: (v: View, tts: SysTts) -> Boolean,
): BindingAdapter =
    linear().setup {
        addType<SystemTts>(R.layout.systts_list_item2)
        onCreate {
            getBinding<SysttsListItemBinding>().apply {
                checkBoxSwitch.setOnClickListener { view ->
                    onSwitchChanged(view, models as List<SysTts>, modelPosition)
                }
                btnEdit.setOnClickListener { measureTimeMillis { onEdit(getModel()) } }
                btnDelete.setOnClickListener { onDelete(getModel()) }
            }
            itemView.setOnClickListener { onItemClick(getModel()) }
            itemView.setOnLongClickListener { onItemLongClick(itemView, getModel()) }
        }

        onBind {
            val model = getModel<SysTts>()
            val binding = getBinding<SysttsListItemBinding>()
            binding.apply {
                tvDescription.text = Html.fromHtml(model.tts?.getDescription())
                tvRaTarget.visibility =
                    if (model.readAloudTarget == ReadAloudTarget.ALL) View.INVISIBLE else View.VISIBLE
            }
        }

        itemDifferCallback = object : ItemDifferCallback {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return (oldItem as SysTts).id == (newItem as SysTts).id
            }

            override fun getChangePayload(oldItem: Any, newItem: Any) = true
        }
    }*/
