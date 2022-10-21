package com.github.jing332.tts_server_android.constant

object CnLocalMap {
    private val languageMapMap = mapOf(
        "zh-CN" to "中文（普通话，简体）",
        "zh-TW" to "中文（台湾，普通话）",
        "zh-HK" to "中文（粤语，繁体）",
        "zh-CN-henan" to "中文（中原官话河南，简体）河南口音",
        "zh-CN-liaoning" to "中文（东北官话，简体）辽宁口音",
        "zh-CN-shaanxi" to "中文（中原官话陕西，简体）陕西口音",
        "zh-CN-shandong" to "中文（冀鲁官话，简体）山东口音",
        "zh-CN-sichuan" to "中文（西南普通话，简体）"
    )

    fun getLanguage(key: String): String {
        return languageMapMap[key] ?: key
    }

    private val map = mapOf(
        "general" to "普通",
        "assistant" to "助手",
        "chat" to "闲聊",
        "customerservice" to "服侍",
        "newscast" to "新闻播报",
        "newscast-casual" to "新闻播报(冷淡)",
        "affectionate" to "温暖亲切",
        "angry" to "生气",
        "calm" to "平静",
        "cheerful" to "欢快",
        "excited" to "激动",
        "friendly" to "温和",
        "hopeful" to "期待",
        "shouting" to "喊叫",
        "terrified" to "害怕",
        "unfriendly" to "冷漠",
        "whispering" to "耳语",
        "empathetic" to "同情",
        "newscast-formal" to "新闻播报(正式)",
        "disgruntled" to "不满",
        "fearful" to "担心",
        "gentle" to "温合文雅",
        "lyrical" to "热情奔放",
        "embarrassed" to "犹豫",
        "sad" to "悲伤",
        "serious" to "严肃",
        "depressed" to "忧伤",
        "envious" to "嫉妒",
        "poetry-reading" to "诗歌朗诵",
        "Default" to "默认",
        //角色(身份) to
        "narration-professional" to "讲故事(专业)",
        "narration-casual" to "讲故事(冷淡)",
        "narration-relaxed" to "讲故事(轻松)",
        "Narration-relaxed" to "讲故事(轻松)",
        "Sports_commentary_excited" to "体育解说(激动)",
        "Sports_commentary" to "体育解说",
        "Advertisement_upbeat" to "广告推销(积极)",
        "YoungAdultFemale" to "女性青年",
        "YoungAdultMale" to "男性青年",
        "OlderAdultFemale" to "年长女性",
        "OlderAdultMale" to "年长男性",
        "SeniorFemale" to "高龄女性",
        "SeniorMale" to "高龄男性",
        "Girl" to "小女孩",
        "Boy" to "小男孩",
        "Narrator" to "旁白",
    )

    fun get(key: String): String {
        return map[key] ?: key
    }
}