package com.github.jing332.tts_server_android.model.rhino.speech_rule

import android.content.Context
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngineContext
import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.Segment

class ScriptEngineContext(
    override val context: Context, override val engineId: String
) : BaseScriptEngineContext(context, engineId) {

    /**
     * 创建一个分词器， 这是一个工厂方法
     * Params:
     * algorithm – 分词算法，传入算法的中英文名都可以，可选列表：
     * 维特比 (viterbi)：效率和效果的最佳平衡
     * 双数组trie树 (dat)：极速词典分词，千万字符每秒
     * 条件随机场 (crf)：分词、词性标注与命名实体识别精度都较高，适合要求较高的NLP任务
     * 感知机 (perceptron)：分词、词性标注与命名实体识别，支持在线学习
     * N最短路 (nshort)：命名实体识别稍微好一些，牺牲了速度
     * Returns:
     * 一个分词器
     */
    @JvmOverloads
    fun newSegment(algorithm: String = "viterbi"): Segment = HanLP.newSegment(algorithm)!!
}