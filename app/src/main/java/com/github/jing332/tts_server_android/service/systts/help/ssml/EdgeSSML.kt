package com.github.jing332.tts_server_android.service.systts.help.ssml

import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.util.StringUtils

class EdgeSSML(var text: String, var property: VoiceProperty) {
    companion object {
        /**
         * 生成多语音的SSML List。Map的Key为对应朗读属性，value[0]=text，value[1]=ssml
         * @param text 需分割的文本
         * @param asideProperty 旁白的朗读属性
         * @param dialogueProperty 对话的朗读属性
         */
        fun genSsmlForMultiVoice(
            text: String,
            asideProperty: VoiceProperty,
            dialogueProperty: VoiceProperty
        ): List<MultiVoiceData> {
            val multiVoice = SsmlTools.splitMultiVoice(text)
            val list = arrayListOf<MultiVoiceData>()
            multiVoice.forEach {
                if (!StringUtils.isSilent(it.text)) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\">")
                    val voiceTag =
                        if (it.isDialogue) dialogueProperty.toString(it.text)
                        else asideProperty.toString(it.text)
                    val pro = if (it.isDialogue) dialogueProperty else asideProperty
                    val ssml = stringBuilder.append("$voiceTag</speak>").toString()
                    list.add(MultiVoiceData(pro, it.text, ssml))
                }
            }

            return list
        }
    }

}

data class MultiVoiceData(val voiceProperty: VoiceProperty, val raText: String, val ssml: String)