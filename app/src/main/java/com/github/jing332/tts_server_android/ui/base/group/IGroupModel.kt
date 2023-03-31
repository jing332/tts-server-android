package com.github.jing332.tts_server_android.ui.base.group

import com.drake.brv.item.ItemDrag
import com.drake.brv.item.ItemExpand
import com.drake.brv.item.ItemHover
import com.google.android.material.checkbox.MaterialCheckBox

interface IGroupModel : ItemExpand, ItemDrag, ItemHover {
    val name: String

    @MaterialCheckBox.CheckedState
    var checkedState: Int

    /**
     * 返回子列表中启用的数量 用于无障碍描述
     */
    val subItemEnabledCount: Int
}