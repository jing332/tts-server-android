package com.github.jing332.tts_server_android.ui.systts.read_rule

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.github.jing332.tts_server_android.data.entities.ReadRule

data class ReadRuleModel(
    val data: ReadRule,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : ItemDrag {
    val title: String
        get() = data.name

    val subTitle: String
        get() = "${data.author} - v${data.version}"

}