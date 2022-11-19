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
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragmentViewModel

class SysTtsConfigListItemAdapter(
    val viewModel: TtsConfigFragmentViewModel,
    var itemList: ArrayList<SysTtsConfigItem>
) :
    Adapter<SysTtsConfigListItemAdapter.ViewHolder>() {
    companion object {
        const val TAG = "SysTtsConfigAdapter"
    }

    fun append(item: SysTtsConfigItem, syncModel: Boolean) {
        if (syncModel) viewModel.ttsCfgLiveData.value?.list?.add(item)
        itemList.add(item)
        notifyItemInserted(itemList.size - 1)
    }

    fun remove(position: Int) {
        viewModel.ttsCfgLiveData.value?.list?.removeAt(position)
        itemList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemList.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAll() {
        itemList.clear()
        notifyDataSetChanged()
    }

    fun update(item: SysTtsConfigItem, position: Int, isUpdateUi: Boolean) {
        viewModel.ttsCfgLiveData.value?.list?.set(position, item)
        itemList[position] = item
        if (isUpdateUi) notifyItemChanged(position)
    }


    var contentClick: ClickListen? = null
    var switchClick: ClickListen? = null
    var editButtonClick: ClickListen? = null
    var delButtonClick: ClickListen? = null
    var itemLongClick: LongClickListen? = null

    interface ClickListen {
        fun onClick(view: View, position: Int)
    }

    interface LongClickListen {
        fun onLongClick(view: View, position: Int): Boolean
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_systts_config, parent, false)

        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = itemList[position]

        holder.apply {
            //是否勾选
            checkBox.isChecked = data.isEnabled
            //显示名称
            tvName.text = data.uiData.displayName
            kotlin.runCatching {
                tvContent.text = Html.fromHtml(data.uiData.setContent(data.voiceProperty))
            }
            //格式
            tvFormat.text = data.voiceProperty.format
            //接口
            tvApiType.text = TtsApiType.toString(data.voiceProperty.api)
            //朗读目标
            ReadAloudTarget.toString(data.readAloudTarget).let {
                tvRaTarget.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
                tvRaTarget.text = it
            }

            checkBox.setOnClickListener { switchClick?.onClick(it, position) }
            tvContent.setOnClickListener { contentClick?.onClick(it, position) }
            tvContent.setOnLongClickListener {
                return@setOnLongClickListener itemLongClick?.onLongClick(
                    itemView,
                    position
                ) ?: false
            }

            itemView.setOnLongClickListener {
                return@setOnLongClickListener itemLongClick?.onLongClick(
                    itemView,
                    position
                ) ?: false
            }
            btnEdit.setOnClickListener {
                editButtonClick?.onClick(it, position)
            }
            btnDelete.setOnClickListener {
                delButtonClick?.onClick(it, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
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