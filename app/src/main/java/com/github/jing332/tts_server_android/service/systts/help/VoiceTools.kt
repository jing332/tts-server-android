package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.util.StringUtils

object VoiceTools {
    /* 分割旁白和对白 */
    private fun splitMultiVoice(s: String): List<ResultTextTag> {
        val voiceTagList = arrayListOf<ResultTextTag>()

        val tmpStr = StringBuilder()
        val strLen = s.length
        s.forEachIndexed { index, char ->
            tmpStr.append(char)
            if (char == '“') {
                voiceTagList.add(ResultTextTag(tmpStr.toString(), false))
                tmpStr.clear()
            } else if (char == '”') {
                voiceTagList.add(ResultTextTag(tmpStr.toString(), true))
                tmpStr.clear()
            } else if (index == strLen - 1)
                voiceTagList.add(ResultTextTag(tmpStr.toString(), false))

        }
        return voiceTagList
    }

    /**
     * 分割为旁白与对话。
     * @param text 需分割的文本
     * @param asideProperty 旁白的朗读属性
     * @param dialogueProperty 对话的朗读属性
     */
    fun splitMultiVoice(
        text: String,
        asideProperty: VoiceProperty,
        dialogueProperty: VoiceProperty
    ): List<ResultMultiVoiceData> {
        val multiVoice = splitMultiVoice(text)
        val list = arrayListOf<ResultMultiVoiceData>()
        multiVoice.forEach {
            if (!StringUtils.isSilent(it.text)) {
                val pro = if (it.isDialogue) dialogueProperty else asideProperty
                list.add(ResultMultiVoiceData(pro, it.text))
            }
        }

        return list
    }
}

data class ResultTextTag(val text: String, val isDialogue: Boolean)
data class ResultMultiVoiceData(
    val voiceProperty: VoiceProperty,
    val raText: String
)