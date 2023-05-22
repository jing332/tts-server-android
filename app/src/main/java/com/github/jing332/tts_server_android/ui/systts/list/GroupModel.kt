package com.github.jing332.tts_server_android.ui.systts.list

import com.drake.brv.annotaion.ItemOrientation
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.ui.base.group.IGroupModel
import com.google.android.material.checkbox.MaterialCheckBox

class GroupModel(
    var data: SystemTtsGroup,

    override var itemSublist: List<Any?>?,
    override var checkedState: Int = MaterialCheckBox.STATE_UNCHECKED,

    override var itemExpand: Boolean = false,
    override var itemGroupPosition: Int = 0,
    override var itemHover: Boolean = true,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : IGroupModel {
    override val name: String
        get() = data.name

    override val subItemEnabledCount: Int
        get() = itemSublist?.filter { (it as ItemModel).data.isEnabled }?.size ?: 0

}