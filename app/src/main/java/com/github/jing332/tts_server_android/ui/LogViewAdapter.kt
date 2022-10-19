package com.github.jing332.tts_server_android.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.R

//显示日志的适配器
class LogViewAdapter(private val dataSet: ArrayList<MyLog>) :
    RecyclerView.Adapter<LogViewAdapter.ViewHolder>() {
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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]
        viewHolder.textView.text = data.msg
        viewHolder.textView.setTextColor(data.toColor())
    }

    override fun getItemCount() = dataSet.size
}
