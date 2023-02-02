package com.github.jing332.tts_server_android.ui.systts.replace

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule

data class ReplaceRuleModel(
    var data: ReplaceRule,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) :
    ItemDrag