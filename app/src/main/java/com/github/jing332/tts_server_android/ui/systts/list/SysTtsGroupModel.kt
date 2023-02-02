package com.github.jing332.tts_server_android.ui.systts.list

import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.ui.systts.BaseGroupModel

data class SysTtsGroupModel(
    override var data: SystemTtsGroup,
    override var itemSublist: List<Any?>?,
) : BaseGroupModel<SystemTtsGroup>(data, itemSublist = itemSublist)