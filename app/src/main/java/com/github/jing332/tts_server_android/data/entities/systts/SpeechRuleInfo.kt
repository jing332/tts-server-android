package com.github.jing332.tts_server_android.data.entities.systts

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.TypeConverters
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.entities.MapConverters
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Parcelize
@TypeConverters(MapConverters::class)
data class SpeechRuleInfo(
    var target: Int = SpeechTarget.ALL,
    var isStandby: Boolean = false,

    @ColumnInfo(defaultValue = "")
    var tag: String = "",
    @ColumnInfo(defaultValue = "")
    var tagRuleId: String = "",

    // 显示在列表右上角的标签名
    @ColumnInfo(defaultValue = "")
    var tagName: String = "",

    // 用于存储tag的数据
    // 例: key=role, value=张三
    @ColumnInfo(defaultValue = "")
    var tagData: Map<String, String> = mutableMapOf(),

    // 用于标识tts配置的唯一性，由脚本处理后将 tag 与 id 返回给程序以找到朗读
    @ColumnInfo(defaultValue = "0")
    var configId: Long = 0L
) : Parcelable {
    val mutableTagData: MutableMap<String, String>
        get() = tagData as MutableMap<String, String>

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

    fun resetTag() {
        tag = ""
        tagRuleId = ""
        tagName = ""
        mutableTagData.clear()
    }

    fun isTagDataEmpty(): Boolean = tagData.filterValues { it.isNotEmpty() }.isEmpty()
}