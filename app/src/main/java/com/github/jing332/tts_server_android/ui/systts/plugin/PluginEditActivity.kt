package com.github.jing332.tts_server_android.ui.systts.plugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditorActivityBinding
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.plugin.debug.PluginLoggerBottomSheetFragment
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.FileUtils.readAllText
import com.github.jing332.tts_server_android.util.readableString
import com.github.jing332.tts_server_android.util.toast
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.tm4e.core.registry.IThemeSource

class PluginEditActivity : BackActivity() {
    private val binding by lazy { SysttsPluginEditorActivityBinding.inflate(layoutInflater) }
    private val vm by viewModels<PluginEditViewModel>()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        vm.init(
            plugin = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: Plugin(),
            defaultCode = resources.openRawResource(R.raw.systts_plugin_template).readAllText()
        )

        vm.codeLiveData.observe(this) {
            binding.editor.setText(it)
        }

        initEditor()
        lifecycleScope.launch(Dispatchers.IO) {
            vm.startSyncServer()
        }
    }

    private fun initEditor() {
        FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(application.assets))

        val themePath = "textmate/quietlight.json"
        val themeRegistry = ThemeRegistry.getInstance()
        themeRegistry.loadTheme(
            ThemeModel(
                IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(themePath), themePath, null
                )
            )
        )
        themeRegistry.setTheme("quietlight")

        GrammarRegistry.getInstance().loadGrammars(languages {
            language("js") {
                grammar = "textmate/javascript/syntaxes/JavaScript.tmLanguage.json"
                defaultScopeName()
                languageConfiguration = "textmate/javascript/language-configuration.json"
            }
        })
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

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_plugin_edit, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
            menu.isGroupDividerEnabled = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        vm.pluginInfo.code = binding.editor.text.toString()
        when (item.itemId) {
            R.id.menu_params -> displayParamsSettings()
            R.id.menu_save_as_file -> saveAsFile()
            R.id.menu_save -> {
                val plugin = try {
                    vm.pluginEngine.evalPluginInfo()
                } catch (e: Exception) {
                    AppDialogs.displayErrorDialog(this, e.readableString)
                    return true
                }

                setResult(RESULT_OK, Intent().apply { putExtra(KeyConst.KEY_DATA, plugin) })
                finish()
            }

            R.id.menu_debug -> {
                debug()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun debug() {
        val bottomSheet = PluginLoggerBottomSheetFragment()
        bottomSheet.show(supportFragmentManager, "PluginLoggerBottomSheetFragment")
        vm.debug()
    }


    private var savedData: ByteArray? = null
    private val getFileUriToSave =
        FileUtils.registerResultCreateDocument(this, "text/javascript") { savedData }

    private fun saveAsFile() {
        savedData = binding.editor.text.toString().toByteArray()
        getFileUriToSave.launch("ttsrv-${vm.pluginInfo.name}.js")
    }

    @Suppress("DEPRECATION")
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)?.let {
                toast("参数仅本次编辑生效")
                vm.updateTTS(it.tts as PluginTTS)
            }
        }


    private fun displayParamsSettings() {
        appDb.pluginDao.update(vm.pluginInfo.apply { code = binding.editor.text.toString() })
        startForResult.launch(Intent(this, PluginTtsEditActivity::class.java).apply {
            putExtra(BaseTtsEditActivity.KEY_BASIC_VISIBLE, false)
            putExtra(BaseTtsEditActivity.KEY_DATA, SystemTts(tts = vm.pluginTTS))
        })
    }
}