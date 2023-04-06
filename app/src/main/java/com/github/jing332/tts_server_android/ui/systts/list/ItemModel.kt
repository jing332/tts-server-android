package com.github.jing332.tts_server_android.ui.systts.list

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

data class ItemModel(
    val data: SystemTts,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : ItemDrag {
    val target: String
        get() {
            return if (data.speechRule.target == SpeechTarget.CUSTOM_TAG)
                appDb.speechRule.getByReadRuleId(data.speechRule.tagRuleId)?.run {
                    tags[data.speechRule.tag] // tag的显示名称
                } ?: app.getString(R.string.systts_not_found_speech_rule_tag)
            else ""
        }

    val api: String
        get() {
            val typeStr = data.tts.getType()
            return if (data.speechRule.isStandby)
                app.getString(R.string.systts_standby) + " - " + typeStr
            else typeStr
        }
}