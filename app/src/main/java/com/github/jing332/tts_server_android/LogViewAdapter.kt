package com.github.jing332.tts_server_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//显示日志的适配器
class LogViewAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<LogViewAdapter.ViewHolder>() {
    //追加日志
    fun append(s: String) {
        if (itemCount > 100) { //日志条目超过便移除第2行日志Item
            dataSet.removeAt(1)
            notifyItemRemoved(1)
        }
        dataSet.add(s)
        notifyItemInserted(dataSet.size)
    }

    fun removeAll() {
        dataSet.removeAll(dataSet)
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
        viewHolder.textView.text = dataSet[position]
    }

    override fun getItemCount() = dataSet.size
}