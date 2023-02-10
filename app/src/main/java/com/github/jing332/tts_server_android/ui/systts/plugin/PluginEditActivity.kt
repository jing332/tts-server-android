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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsPluginDebugResultBottomsheetBinding
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditorActivityBinding
import com.github.jing332.tts_server_android.help.plugin.LogOutputter
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.readableString
import com.github.jing332.tts_server_android.util.rootCause
import com.github.jing332.tts_server_android.util.runOnIO
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.script.ScriptException
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IThemeSource

class PluginEditActivity : BackActivity() {
    private val binding by lazy { SysttsPluginEditorActivityBinding.inflate(layoutInflater) }
    private val vm by viewModels<PluginEditViewModel>()

    private val debugViewBinding by lazy {
        SysttsPluginDebugResultBottomsheetBinding.inflate(
            LayoutInflater.from(this), null, false
        )
    }

    @Suppress("DEPRECATION")
    private val debugBottomSheetDialog by lazy {
        BottomSheetDialog(this).apply {
            debugViewBinding.root.minimumHeight =
                this@PluginEditActivity.windowManager.defaultDisplay.height
            setContentView(debugViewBinding.root)
        }
    }

    private lateinit var mData: Plugin
    private var mTts: PluginTTS = PluginTTS()


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

        initEditor()

        // Debug日志输出
        val output = LogOutputter.OutputInterface { msg, level ->
            synchronized(this@PluginEditActivity) {
                val span = SpannableString(msg).apply {
                    setSpan(
                        ForegroundColorSpan(LogLevel.toColor(level)),
                        0, msg.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                runOnUiThread {
                    debugViewBinding.tvLog.append("\n")
                    debugViewBinding.tvLog.append(span)
                }
            }
        }

        LogOutputter.addTarget(output)
    }

    private fun initEditor() {
        FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(application.assets))

        val path = "textmate/quietlight.json"

        val themeRegistry = ThemeRegistry.getInstance()
        themeRegistry.loadTheme(
            ThemeModel(
                IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                )
            )
        )
        themeRegistry.setTheme("quietlight")

        GrammarRegistry.getInstance().loadGrammars(
            languages {
                language("js") {
                    grammar = "textmate/javascript/syntaxes/JavaScript.tmLanguage.json"
                    defaultScopeName()
                    languageConfiguration = "textmate/javascript/language-configuration.json"
                }
            }
        )
        binding.editor.setEditorLanguage(TextMateLanguage.create("source.js", true))
        ensureTextmateTheme()
    }

    private fun ensureTextmateTheme() {
        val editor = binding.editor
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_plugin_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mData.code = binding.editor.text.toString()
        when (item.itemId) {
            R.id.menu_params -> displayParamsSettings()
            R.id.menu_save -> {
                val plugin = try {
                    vm.pluginEngine.evalPluginInfo()
                } catch (e: Exception) {
                    displayDebugMessage(e.readableString).setTextColor(Color.RED)
                    return true
                }

                setResult(RESULT_OK, Intent().apply { putExtra(KeyConst.KEY_DATA, plugin) })
                finish()
            }

            R.id.menu_debug -> {
                val tv = displayDebugMessage()

                val plugin = try {
                    vm.pluginEngine.evalPluginInfo()
                } catch (e: Exception) {
                    writeDebugErrorLine(e)
                    return true
                }
                tv.append("\n" + plugin.toString().replace(", ", "\n"))

                lifecycleScope.runOnIO {
                    val sampleRate = try {
                        vm.pluginEngine.getSampleRate(mTts.locale, mTts.voice)
                    } catch (e: Exception) {
                        writeDebugErrorLine(e)
                    }
                    LogOutputter.writeLine("采样率: $sampleRate")

                    val audio = try {
                        vm.pluginEngine.getAudio(
                            "测试文本", mTts.locale, mTts.voice, mTts.rate,
                            mTts.volume, mTts.pitch
                        )
                    } catch (e: Exception) {
                        writeDebugErrorLine(e)
                        return@runOnIO
                    }
                    if (audio == null) {
                        LogOutputter.writeLine("\n音频为空！", LogLevel.ERROR)
                    } else {
                        LogOutputter.writeLine("\n音频大小: ${audio.size / 1024}KiB")
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)?.let {
                toast("参数仅本次编辑生效")
                mTts = it.tts as PluginTTS
            }
        }

    private fun displayParamsSettings() {
        appDb.pluginDao.update(mData.apply { code = binding.editor.text.toString() })
        startForResult.launch(Intent(this, PluginTtsEditActivity::class.java).apply {
            putExtra(BaseTtsEditActivity.KEY_BASIC_VISIBLE, false)
            putExtra(BaseTtsEditActivity.KEY_DATA, SystemTts(tts = PluginTTS(plugin = mData)))
        })
    }


    private fun writeDebugErrorLine(e: Exception) {
        val errStr = if (e is ScriptException) {
            "第 ${e.lineNumber} 行错误：${e.rootCause?.message ?: e}"
        } else {
            e.message + "($e)"
        }
        LogOutputter.writeLine(errStr, LogLevel.ERROR)

    }

    private fun displayDebugMessage(msg: String = "", e: Exception? = null): TextView {
        debugViewBinding.tvLog.text = msg

        debugBottomSheetDialog.show()
        return debugViewBinding.tvLog
    }

}