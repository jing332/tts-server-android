package com.github.jing332.tts_server_android.ui.view.widget.spinner

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable

abstract class BaseMaterialSpinnerAdapter<T>(context: Context, val items: List<T>) :
    BaseAdapter(),
    Filterable {
    private val _filter: Filter by lazy {
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

            }
        }
    }
    val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    var selectedItemPosition = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): T = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter = _filter
}