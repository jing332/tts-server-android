package com.github.jing332.tts_server_android.ui.custom.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.util.longToastOnUi

//显示日志的适配器
class LogListItemAdapter(private val dataSet: ArrayList<MyLog>) :
    RecyclerView.Adapter<LogListItemAdapter.ViewHolder>() {
    //追加日志
    fun append(data: MyLog) {
        if (itemCount > 100) { //日志条目超过便移除第2行日志Item
            dataSet.removeAt(1)
            notifyItemRemoved(1)
        }
        dataSet.add(data)
        notifyItemInserted(dataSet.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAll() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    //用来构建Item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.textView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.log_itemlist, viewGroup, false)

        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataSet[position]
//        holder.textView.text =
        holder.textView.text = Html.fromHtml(data.msg)

        holder.textView.setTextColor(data.toColor())
        holder.itemView.setOnLongClickListener {
            val cm = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val mClipData = ClipData.newPlainText("log", holder.textView.text.trim())
            cm?.setPrimaryClip(mClipData)
            it.context.longToastOnUi(it.context.getString(R.string.copied))
            true
        }
    }

    override fun getItemCount() = dataSet.size
}
