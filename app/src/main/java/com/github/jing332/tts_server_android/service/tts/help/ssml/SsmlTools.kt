package com.github.jing332.tts_server_android.service.tts.help.ssml

object SsmlTools {
/*    fun genEdgeSsmlVoice(
        voiceName: String,
        pitch: String,
        rate: String,
        volume: String,
        text: String
    ): String {

    }*/


    /* 分割旁白和对白 */
    fun splitMultiVoice(s: String): List<SsmlVoiceTag> {
        val voiceTagList = arrayListOf<SsmlVoiceTag>()

        val tmpStr = StringBuilder()
        val strLen = s.length
        s.forEachIndexed { index, char ->
            print(char)
            tmpStr.append(char)
            if (char == '“') {
                voiceTagList.add(SsmlVoiceTag(tmpStr.toString(), false))
                tmpStr.clear()
            } else if (char == '”') {
                voiceTagList.add(SsmlVoiceTag(tmpStr.toString(), true))
                tmpStr.clear()
            } else if (index == strLen - 1)
                voiceTagList.add(SsmlVoiceTag(tmpStr.toString(), false))

        }
        return voiceTagList
    }
}

data class SsmlVoiceTag(val text: String, val isDialogue: Boolean)

/*
object Type {
    const val ASIZE = 0
    const val DIALOGUE = 1
}*/
