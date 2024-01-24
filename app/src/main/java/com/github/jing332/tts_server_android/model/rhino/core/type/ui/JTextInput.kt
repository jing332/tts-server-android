package com.github.jing332.tts_server_android.model.rhino.core.type.ui

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.widget.FrameLayout
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView

@Suppress("unused")
@SuppressLint("ViewConstructor")
class JTextInput(context: Context, val hint: CharSequence? = null) : FrameLayout(context) {

    interface OnTextChangedListener {
        fun onChanged(text: CharSequence)
    }

    private val listeners = mutableSetOf<OnTextChangedListener>()

    fun addTextChangedListener(listener: OnTextChangedListener) {
        listeners.add(listener)
    }

    fun removeTextChangedListener(listener: OnTextChangedListener) {
        listeners.remove(listener)
    }

    private var mOnTextChangedListeners: OnTextChangedListener? = null

    fun setOnTextChangedListener(listener: OnTextChangedListener) {
        listeners.add(listener)
    }

    init {
        val compose = ComposeView(context)
        this.addView(compose)
        compose.setContent {
            Content()
        }

    }

    val text: Editable by lazy { MyEditable() }
    private var mText by mutableStateOf("")

    private var mMaxLines by mutableIntStateOf(Int.MAX_VALUE)
    var maxLines: Int
        get() = mMaxLines
        set(value) {
            mMaxLines = value
        }


    @Composable
    fun Content() {
        OutlinedTextField(
            value = mText,
            onValueChange = {
                mText = it
                for (listener in listeners) {
                    listener.onChanged(it)
                }
            },
            maxLines = mMaxLines,
            label = { Text(hint.toString()) }
        )
    }

    inner class MyEditable() : Editable {
        override val length = mText.length

        override fun get(index: Int): Char {
            return mText[index]
        }

        fun set(text: CharSequence) {
            mText = text.toString()
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            TODO("Not yet implemented")
        }

        override fun getChars(start: Int, end: Int, dest: CharArray?, destoff: Int) {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
            TODO("Not yet implemented")
        }

        override fun getSpanStart(tag: Any?): Int {
            TODO("Not yet implemented")
        }

        override fun getSpanEnd(tag: Any?): Int {
            TODO("Not yet implemented")
        }

        override fun getSpanFlags(tag: Any?): Int {
            TODO("Not yet implemented")
        }

        override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int {
            TODO("Not yet implemented")
        }

        override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
            TODO("Not yet implemented")
        }

        override fun removeSpan(what: Any?) {
            TODO("Not yet implemented")
        }

        override fun append(text: CharSequence?): Editable {
            mText += text
            return this
        }

        override fun append(text: CharSequence?, start: Int, end: Int): Editable {
            TODO("Not yet implemented")
        }

        override fun append(text: Char): Editable {
            mText += text
            return this
        }

        override fun replace(
            st: Int,
            en: Int,
            source: CharSequence?,
            start: Int,
            end: Int
        ): Editable {
            TODO("Not yet implemented")
        }

        override fun replace(st: Int, en: Int, text: CharSequence?): Editable {
            TODO("Not yet implemented")
        }

        override fun insert(where: Int, text: CharSequence?, start: Int, end: Int): Editable {
            TODO("Not yet implemented")
        }

        override fun insert(where: Int, text: CharSequence?): Editable {
            TODO("Not yet implemented")
        }

        override fun delete(st: Int, en: Int): Editable {
            TODO("Not yet implemented")
        }

        override fun clear() {
            mText = ""
        }

        override fun clearSpans() {
            TODO("Not yet implemented")
        }

        override fun setFilters(filters: Array<out InputFilter>?) {
            TODO("Not yet implemented")
        }

        override fun getFilters(): Array<InputFilter> {
            TODO("Not yet implemented")
        }

    }
}