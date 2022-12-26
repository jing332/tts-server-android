package com.github.jing332.tts_server_android.ui.systts.list.my_group

import com.drake.brv.item.ItemExpand
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.google.android.material.checkbox.MaterialCheckBox

class RvGroupModel(
    var data: SystemTtsGroup,
    var checkedState: Int = MaterialCheckBox.STATE_UNCHECKED,
    override var itemExpand: Boolean = false,
    override var itemGroupPosition: Int = 0,
    override var itemSublist: List<Any?>?
) : ItemExpand {
    val name: String
        get() = data.name
}