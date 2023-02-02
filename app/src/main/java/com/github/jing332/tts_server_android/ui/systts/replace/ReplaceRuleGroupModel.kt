package com.github.jing332.tts_server_android.ui.systts.replace

import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.ui.systts.BaseGroupModel

data class ReplaceRuleGroupModel(
    override var data: ReplaceRuleGroup,
    override var itemSublist: List<Any?>?,
    override var itemExpand: Boolean = false,
    override var checkedState: Int
) :
    BaseGroupModel<ReplaceRuleGroup>(
        data = data,
        itemSublist = itemSublist,
        itemExpand = itemExpand,
        checkedState = checkedState
    )