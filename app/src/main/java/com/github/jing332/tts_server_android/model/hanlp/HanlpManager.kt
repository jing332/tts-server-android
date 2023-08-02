package com.github.jing332.tts_server_android.model.hanlp

import android.util.Log
import com.hankcs.hanlp.HanLP
import java.io.File

object HanlpManager {
    const val TAG = "HanlpManager"

    fun test(): Boolean {
        kotlin.runCatching {
            HanLP.newSegment().seg("test, 测试")
            return true
        }

        return false
    }

    fun initDir(dir: String) {
        val cfgClz = HanLP.Config::class.java
        for (field in cfgClz.declaredFields) {
            if (field.type == String::class.java) {
                field.isAccessible = true
                val value = field.get(null) as String
                val newValue = dir + File.separator + value.removePrefix("data/")
                field.set(null, newValue)
                Log.d(TAG, "set config: $newValue")
            }
        }

//        HanLP.extractWords()

        /*        val ss = """
                   “当然，来，我让你看看上次文明的努力。”墨子领着汪淼走到观星台一角，大地在他们下面伸展开来，像一块沧桑的旧皮革，墨子将一架小望远镜对准下面大地上的一个目标，然后让汪淼看。汪淼将眼晴凑到目镜上，看到一个奇异的东西，那是一具骷髅，在晨光中呈雪白色，看上去结构很精致。最令人惊奇的是这骷髅站立着，那姿势很是优雅高贵，一只手抬到颚下，似乎在抚摸着那已不存在的胡须，它的头微仰，仿佛在向天地发问。
                    “那是孔子。”墨子指着那个方向说，“他认为，一切都要合乎礼，宇宙万物都不例外。他于是创造了一套宇宙的礼法系统，企图据此预测太阳的运行。”
                """.trimIndent()

                HanLP.newSegment()

                for (terms in HanLP.newSegment().seg2sentence(ss)) {
                    val sb = StringBuilder()
                    for (term in terms) {
                        sb.append(term).append(" | ")
                    }
                    Log.d(TAG, sb.toString())
                }

                HanLP.newSegment().seg()*/
    }
}