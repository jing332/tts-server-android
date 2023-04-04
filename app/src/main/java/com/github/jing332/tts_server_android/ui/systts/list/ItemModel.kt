package com.github.jing332.tts_server_android.ui.systts.list

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

data class ItemModel(
    val data: SystemTts,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : ItemDrag {
    val target: String
        get() {
            return if (data.readAloudTarget == ReadAloudTarget.CUSTOM_TAG) {
                appDb.speechRule.getByReadRuleId(data.speechRule.tagRuleId)?.run {
                    tags[data.speechRule.tag] // tag的显示名称
                } ?: data.speechRule.tag
                ""
            } else
                ReadAloudTarget.toText(data.readAloudTarget)
        }

    val api: String
        get() = data.tts.getType()
}