package com.github.jing332.tts_server_android.data.entities.systts

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Parcelize
data class SpeechRuleInfo(
    var target: Int = SpeechTarget.ALL,
    var isStandby: Boolean = false,

    @ColumnInfo(defaultValue = "")
    var tag: String = "",
    @ColumnInfo(defaultValue = "")
    var tagRuleId: String = "",
) :
    Parcelable {
    @IgnoredOnParcel
    @Ignore
    @Transient
    var standbyTts: ITextToSpeechEngine? = null

    /**
     * 判断tag是否相同
     * @return 相同
     */
    fun isTagSame(rule: SpeechRuleInfo): Boolean {
        return tag == rule.tag && tagRuleId == rule.tagRuleId
    }
}