package com.github.jing332.tts_server_android.ui.systts.replace

import com.drake.brv.annotaion.ItemOrientation
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.ui.base.group.IGroupModel
import com.google.android.material.checkbox.MaterialCheckBox

data class GroupModel(
    var data: ReplaceRuleGroup,

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
        get() = itemSublist?.filter { (it as ReplaceRuleModel).data.isEnabled }?.size ?: 0

}

