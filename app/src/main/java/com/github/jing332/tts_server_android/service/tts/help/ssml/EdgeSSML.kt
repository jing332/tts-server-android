package com.github.jing332.tts_server_android.service.tts.help.ssml

class EdgeSSML(var text: String, var property: VoiceProperty) {
    companion object {
        fun genSsmlForMultiVoice(
            text: String,
            asideProperty: VoiceProperty,
            dialogueProperty: VoiceProperty
        ): LinkedHashMap<String, String> {
            val multiVoice = SsmlTools.splitMultiVoice(text)
            val map = LinkedHashMap<String, String>() //有序map
            multiVoice.forEach {
                val stringBuilder = StringBuilder()
                stringBuilder.append("<speak  xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\">")
                val voiceTag =
                    if (it.isDialogue) dialogueProperty.toString(it.text) else asideProperty.toString(
                        it.text
                    )
                map[it.text] = stringBuilder.append("$voiceTag</speak>").toString()
            }

            return map
        }
    }

}