package com.github.jing332.tts_server_android.ui.systts.replace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import com.github.jing332.tts_server_android.databinding.ItemReplaceRuleBinding

class ReplaceRuleItemAdapter(val itemList: ArrayList<ReplaceRuleItemData>) :
    RecyclerView.Adapter<ReplaceRuleItemAdapter.ViewHolder>() {


    fun append(data: ReplaceRuleItemData) {
        itemList.add(data)
        notifyItemInserted(itemList.size)
    }

    fun remove(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReplaceRuleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = itemList[position]
        holder.binding.apply {
            checkBox.isChecked = data.isEnabled
            checkBox.text = data.name

            checkBox.setOnClickListener { callBack?.switch(position, checkBox.isChecked) }
            btnEdit.setOnClickListener { callBack?.edit(position) }
            btnDelete.setOnClickListener { callBack?.delete(position) }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(val binding: ItemReplaceRuleBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    var callBack: CallBack? = null

    interface CallBack {
        fun switch(position: Int, isSelected: Boolean)
        fun edit(position: Int)
        fun delete(position: Int)
    }
}
