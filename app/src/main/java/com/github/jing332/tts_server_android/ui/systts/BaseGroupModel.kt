package com.github.jing332.tts_server_android.ui.systts

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.drake.brv.item.ItemExpand
import com.drake.brv.item.ItemHover
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup
import com.google.android.material.checkbox.MaterialCheckBox

abstract class BaseGroupModel<T : AbstractListGroup>(
    open var data: T,
    open var checkedState: Int = MaterialCheckBox.STATE_UNCHECKED,

    override var itemSublist: List<Any?>?,
    override var itemExpand: Boolean = false,

    override var itemGroupPosition: Int = 0,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL,
    override var itemHover: Boolean = true,
) : ItemExpand, ItemDrag, ItemHover {
    val name: String
        get() = data.name
}