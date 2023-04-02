package com.github.jing332.tts_server_android.speech

import com.github.jing332.tts_server_android.model.speech.ITextToSpeechAdapter
import com.github.jing332.tts_server_android.model.speech.TtsSynthesizer
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.tts.MsTTS
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TtsTest {
    @Test
    fun tts() = runBlocking {
        val synthesizer = TtsSynthesizer()
        synthesizer.ttsAdapter = object : ITextToSpeechAdapter<ITextToSpeechEngine>() {
            override suspend fun getAudio(
                tts: ITextToSpeechEngine,
                text: String,
                sysRate: Int,
                sysPitch: Int
            ): ByteArray? {
                return tts.getAudio(text)
            }

            private val narrationConfig by lazy { MsTTS() }
            private val dialogueConfig by lazy { MsTTS() }

            override fun handleText(text: String): List<TextWithTTS<ITextToSpeechEngine>> {
                return text.split(",").mapIndexed { i, v ->
                    TextWithTTS(if (i % 2 == 0) narrationConfig else dialogueConfig, v)
                }
            }
        }

        synthesizer.synthesizeText(
            "测试文本, 测试2,测试文本3, 测试4",
            sysRate = 50,
            sysPitch = 50
        ) { audio, txtTts ->
            println("$audio, $txtTts")
        }
    }
}