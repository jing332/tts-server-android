package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.lengthOfChinese
import kotlin.random.Random

object VoiceTools {
    /* 分割旁白和对白 */
    private fun splitMultiVoice(s: String): List<ResultTextTag> {
        val voiceTagList = mutableListOf<ResultTextTag>()

        val tmpStr = StringBuilder()
        var isInDialog = false
        val strLen = s.length
        s.forEachIndexed { index, char ->
            tmpStr.append(char)
            if (char == '“') { //引号开头
                isInDialog = true
                voiceTagList.add(ResultTextTag(tmpStr.toString(), false))
                tmpStr.clear()
            } else if (char == '”') { //引号结尾
                isInDialog = false
                tmpStr.deleteAt(tmpStr.length - 1)
                voiceTagList.add(ResultTextTag(tmpStr.toString(), true))
                tmpStr.clear()
            } else if (index == strLen - 1) { //末尾
                voiceTagList.add(
                    ResultTextTag(tmpStr.toString(), isInDialog)
                )
            }

        }
        return voiceTagList
    }

    /**
     * 分割为旁白与对话。为list则自动随机
     * @param text 需分割的文本
     * @param asideProperty 旁白的朗读属性
     * @param dialogueProperty 对话的朗读属性
     */
    fun splitMultiVoice(
        text: String,
        asideProperty: List<BaseTTS>,
        dialogueProperty: List<BaseTTS>,
        minDialogueLen: Int
    ): List<ResultMultiVoiceData> {
        val random = Random(System.currentTimeMillis())
        val asideSize = asideProperty.size
        val dialogSize = dialogueProperty.size

        val multiVoice = splitMultiVoice(text)
        val list = arrayListOf<ResultMultiVoiceData>()
        multiVoice.forEach {
            if (!StringUtils.isSilent(it.text)) {
                val pro =
                    if (it.isDialogue && it.text.lengthOfChinese() >= minDialogueLen)
                        dialogueProperty[random.nextInt(dialogSize)]
                    else asideProperty[random.nextInt(asideSize)]

                list.add(ResultMultiVoiceData(pro, it.text, it.isDialogue))
            }
        }

        return list
    }
}

data class ResultTextTag(val text: String, val isDialogue: Boolean)
data class ResultMultiVoiceData(
    val tts: BaseTTS, val speakText: String, val isDialogue: Boolean,
)