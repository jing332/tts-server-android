package com.github.jing332.tts_server_android.ui.systts.plugin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.databinding.SysttsPluginDebugResultBottomsheetBinding
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditorActivityBinding
import com.github.jing332.tts_server_android.help.plugin.LogOutputter
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.runOnIO
import com.google.android.material.bottomsheet.BottomSheetDialog

class PluginEditActivity : BackActivity() {
    private val binding by lazy { SysttsPluginEditorActivityBinding.inflate(layoutInflater) }
    private val vm by viewModels<PluginEditViewModel>()
    private lateinit var mData: Plugin

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mData = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: Plugin()
        if (mData.code.isBlank()) {
            mData.code = resources.openRawResource(R.raw.systts_plugin_template).readAllText()
        }
        binding.editor.setText(mData.code)

        vm.setData(mData)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_plugin_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mData.code = binding.editor.text.toString()
        when (item.itemId) {
            R.id.menu_save -> {
                val plugin = try {
                    vm.pluginEngine.evalPluginInfo()
                } catch (e: Exception) {
                    displayDebugMessage(e.stackTraceToString()).setTextColor(Color.RED)
                    return true
                }

                setResult(RESULT_OK, Intent().apply { putExtra(KeyConst.KEY_DATA, plugin) })
                finish()
            }

            R.id.menu_debug -> {
                val tv = displayDebugMessage()
                val output = LogOutputter.OutputInterface { msg, level ->
                    val span = SpannableString(msg).apply {
                        setSpan(
                            ForegroundColorSpan(LogLevel.toColor(level)),
                            0, msg.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    tv.append("\n")
                    tv.append(span)
                }
                LogOutputter.addTarget(output)

                val plugin = try {
                    vm.pluginEngine.evalPluginInfo()
                } catch (e: Exception) {
                    LogOutputter.writeLine(e.stackTraceToString(), LogLevel.ERROR)
                    LogOutputter.removeTarget(output)
                    return true
                }
                tv.append("\n" + plugin.toString().replace(", ", "\n"))


                val sampleRate = try {
                    vm.pluginEngine.getSampleRate()
                } catch (e: Exception) {
                    LogOutputter.writeLine(e.stackTraceToString(), LogLevel.ERROR)
                }
                LogOutputter.writeLine("采样率: $sampleRate")

                lifecycleScope.runOnIO {
                    val audio = try {
                        vm.pluginEngine.getAudio("测试文本", 1)
                    } catch (e: Exception) {
                        LogOutputter.writeLine(e.stackTraceToString(), LogLevel.ERROR)
                        LogOutputter.removeTarget(output)
                        return@runOnIO
                    }
                    if (audio == null) {
                        tv.append("\n音频为空！")
                    } else {
                        tv.append("\n音频大小: ${audio.size / 1024}KiB")
                    }

                    LogOutputter.removeTarget(output)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun displayDebugMessage(msg: String = ""): TextView {
        val viewBinding = SysttsPluginDebugResultBottomsheetBinding.inflate(
            LayoutInflater.from(this),
            null,
            false
        )
        viewBinding.tvLog.text = msg

        BottomSheetDialog(this).apply {
            viewBinding.tvLog.height = this@PluginEditActivity.windowManager.defaultDisplay.height
            setContentView(viewBinding.root)
            show()
        }
        return viewBinding.tvLog
    }

}