package com.github.jing332.tts_server_android.ui.view.widget

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AppMaterialDialogBuilder(context: Context, theme: Int = 0) :
    MaterialAlertDialogBuilder(context, theme) {
    override fun setTitle(title: CharSequence?): AppMaterialDialogBuilder {
        return super.setTitle(title) as AppMaterialDialogBuilder
    }

    override fun setTitle(titleId: Int): AppMaterialDialogBuilder {
        return super.setTitle(titleId) as AppMaterialDialogBuilder
    }

    override fun setIcon(icon: Drawable?): AppMaterialDialogBuilder {
        return super.setIcon(icon) as AppMaterialDialogBuilder
    }

    override fun setIcon(iconId: Int): AppMaterialDialogBuilder {
        return super.setIcon(iconId) as AppMaterialDialogBuilder
    }

    override fun setIconAttribute(attrId: Int): AppMaterialDialogBuilder {
        return super.setIconAttribute(attrId) as AppMaterialDialogBuilder
    }

    override fun setView(view: Int): AppMaterialDialogBuilder {
        return super.setView(view) as AppMaterialDialogBuilder
    }

    override fun setView(view: View?): AppMaterialDialogBuilder {
        return super.setView(view) as AppMaterialDialogBuilder
    }

    override fun setPositiveButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setPositiveButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setNegativeButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNegativeButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setNeutralButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNeutralButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setCancelable(cancelable: Boolean): AppMaterialDialogBuilder {
        return super.setCancelable(cancelable) as AppMaterialDialogBuilder
    }

    override fun setMessage(messageId: Int): AppMaterialDialogBuilder {
        return super.setMessage(messageId) as AppMaterialDialogBuilder
    }

    lateinit var dialog: AlertDialog

    override fun setSingleChoiceItems(
        items: Array<out CharSequence>?,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ): MaterialAlertDialogBuilder {
        if (items == null) return this

        val chooseView = ListChooseView(context)
        chooseView.setItems(items.map { it.toString() }, checkedItem)
        chooseView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            listener?.onClick(dialog, position)
        }
        setView(chooseView)

        return this
    }

    override fun create(): AlertDialog {
        dialog = super.create()
        return dialog
    }
}