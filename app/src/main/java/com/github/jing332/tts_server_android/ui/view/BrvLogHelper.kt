package com.github.jing332.tts_server_android.ui.view

import android.annotation.SuppressLint
import android.text.Html
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ItemLogBinding
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Suppress("DEPRECATION")
class BrvLogHelper(
    val rv: RecyclerView,
    private val isHtmlEnabled: Boolean = false,
    stackFromEnd: Boolean = true
) : RecyclerView.OnScrollListener() {
    private var brv: BindingAdapter
    private var fab: FloatingActionButton? = null

    init {
        brv = setup()
        brv.models = mutableListOf<AppLog>()

        (rv.layoutManager as LinearLayoutManager).stackFromEnd = stackFromEnd

        rv.setOnScrollListener(this)
    }

    private var isScrolledToBottom = false
        set(value) {
            field = value

            if (value) fab?.hide() else fab?.show()
        }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val layoutManager = rv.layoutManager as LinearLayoutManager
        val lastPosition = layoutManager.findLastVisibleItemPosition()

        isScrolledToBottom = layoutManager.itemCount - lastPosition <= 5
    }

    @Suppress("DEPRECATION")
    private fun setup(): BindingAdapter {
        return rv.linear().setup {
            addType<AppLog>(R.layout.item_log)

            onCreate {
                getBinding<ItemLogBinding>().apply {
                    textView.setOnLongClickListener {
                        val model = getModel<AppLog>()
                        ClipboardUtils.copyText(model.msg)
                        context.toast(R.string.copied)
                        true
                    }
                }
            }

            onBind {
                getBinding<ItemLogBinding>().apply {
                    val model: AppLog = getModel()

                    textView.setTextColor(model.toColor())
                    textView.text = if (isHtmlEnabled) {
                        Html.fromHtml(model.msg)
                    } else model.msg
                }
            }
        }
    }

    fun append(log: AppLog) {
        brv.addModels(listOf(log))

        if (isScrolledToBottom)
            rv.scrollToPosition(brv.modelCount - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAll() {
        brv.mutable.clear()
        brv.notifyDataSetChanged()
    }

    fun bindFloatingActionButton(fab: FloatingActionButton) {
        this.fab = fab
        fab.hide()
        fab.setOnClickListener {
            rv.smoothScrollToPosition(brv.modelCount - 1)
        }
    }

}