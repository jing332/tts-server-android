package com.github.jing332.tts_server_android.ui.custom.adapter

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.data.entities.SysTts

class SysTtsConfigListItemAdapter(
    val items: MutableList<SysTts> = mutableListOf()
) :
    Adapter<SysTtsConfigListItemAdapter.ViewHolder>() {
    companion object {
        const val TAG = "SysTtsConfigAdapter"
    }


    fun setItems(items: List<SysTts>) {
        if (items.isNotEmpty()) this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun append(item: SysTtsConfigItem) {
//        itemList.add(item)
//        notifyItemInserted(itemList.size - 1)
    }

    fun remove(position: Int) {
//        itemList.removeAt(position)
//        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, itemList.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAll() {
//        itemList.clear()
//        notifyDataSetChanged()
    }

    fun update(item: SysTtsConfigItem, position: Int, isUpdateUi: Boolean = true) {
//        itemList[position] = item
//        if (isUpdateUi) notifyItemChanged(position)
    }


    interface CallBack {
        fun onSwitchClick(view: View?,position: Int)
        fun onContentClick(data: SysTts)
        fun onEdit(data: SysTts)
        fun onDelete(data: SysTts)
        fun onItemLongClick(view: View, data: SysTts): Boolean
    }

    var callBack: CallBack? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_systts_config, parent, false)

        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        holder.apply {
            //是否勾选
            checkBox.isChecked = data.isEnabled
            //显示名称
            tvName.text = data.uiData.displayName
            kotlin.runCatching {
                tvContent.text = Html.fromHtml(data.uiData.setContent(data.msTtsProperty))
            }
            data.msTtsProperty?.let {
                //格式
                tvFormat.text = it.format
                //接口
                tvApiType.text = TtsApiType.toString(it.api)
            }

            //朗读目标
            ReadAloudTarget.toString(data.readAloudTarget).let {
                tvRaTarget.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
                tvRaTarget.text = it
            }

            checkBox.setOnClickListener {
                callBack?.onSwitchClick(it, position)
            }
            tvContent.setOnClickListener { callBack?.onContentClick(data) }
            tvContent.setOnLongClickListener {
                return@setOnLongClickListener callBack?.onItemLongClick(it, data) ?: false
            }
            itemView.setOnLongClickListener {
                return@setOnLongClickListener callBack?.onItemLongClick(it, data) ?: false
            }
            btnEdit.setOnClickListener {
                callBack?.onEdit(data)
            }
            btnDelete.setOnClickListener {
                callBack?.onDelete(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox

        val tvName: TextView
        val tvContent: TextView
        val tvFormat: TextView
        val btnEdit: ImageView
        val btnDelete: ImageView

        val tvRaTarget: TextView
        val tvApiType: TextView

        init {
            checkBox = view.findViewById(R.id.checkBox_switch)
            tvName = view.findViewById(R.id.tv_name)
            tvContent = view.findViewById(R.id.tv_content)
            tvFormat = view.findViewById(R.id.tv_format)
            btnEdit = view.findViewById(R.id.btn_edit)
            btnDelete = view.findViewById(R.id.btn_delete)

            tvApiType = view.findViewById(R.id.tv_apiType)
            tvRaTarget = view.findViewById(R.id.tv_raTarget)
        }
    }
}