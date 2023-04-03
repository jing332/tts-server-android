package com.github.jing332.tts_server_android.ui.systts

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.SysttsBaseScriptEditorActivityBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.PluginConfig
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.CodeEditorHelper
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.parcelize.Parcelize

abstract class BaseScriptEditorActivity : BackActivity() {
    private lateinit var mEditorHelper: CodeEditorHelper
    private val baseBinding by lazy { SysttsBaseScriptEditorActivityBinding.inflate(layoutInflater) }

    val editor: CodeEditor by lazy { baseBinding.editor }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(baseBinding.root)

        mEditorHelper = CodeEditorHelper(this, baseBinding.editor)
        mEditorHelper.initEditor()
        mEditorHelper.setTheme(AppConfig.codeEditorTheme)

        /*  if (PluginConfig.isRemoteSyncEnabled) {
              lifecycleScope.launch(Dispatchers.IO) {
                  kotlin.runCatching {
                      vm.startSyncServer(
                          onPush = {
                              App.localBroadcast.sendBroadcast(Intent(PluginTtsEditActivity.ACTION_FINISH))
                              baseBinding.editor.setText(it)
                          },
                          onPull = { baseBinding.editor.text.toString() }, onDebug = { debug() },
                          onUI = { previewUi() }
                      )
                  }.onFailure {
                      this@PluginEditActivity.displayErrorDialog(it)
                  }
              }
          }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_base_script_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.menu_word_wrap)?.isChecked = AppConfig.isCodeEditorWordWrapEnabled
            findItem(R.id.menu_remote_sync)?.isChecked = PluginConfig.isRemoteSyncEnabled
        }
        return super.onPrepareOptionsMenu(menu)
    }

    abstract fun updateCode(code: String)
    abstract fun clearPluginCache(): Boolean

    /**
     * @return 文件名
     */
    abstract fun onSaveAsFile(): String

    /**
     * @return 是否保存
     */
    abstract fun onSave(): Boolean

    private var savedData: ByteArray? = null
    private val fileSaver =
        FileUtils.registerResultCreateDocument(this, "text/javascript") { savedData }

    private fun saveAsFile() {
        savedData = editor.text.toString().toByteArray()
        fileSaver.launch(onSaveAsFile())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        updateCode(baseBinding.editor.text.toString())
        when (item.itemId) {
            R.id.menu_clear_cache -> {
                if (clearPluginCache()) longToast(R.string.cleared)
            }

            /* R.id.menu_remote_sync -> {
                 val view = FrameLayout(this)
                 val viewbaseBinding =
                     SysttsPluginSyncSettingsbaseBinding.inflate(layoutInflater, view, true)
                 viewbaseBinding.apply {
                     tvTip.text =
                         Html.fromHtml(getString(R.string.plugin_sync_service_tip))

                     sw.isChecked = PluginConfig.isRemoteSyncEnabled
                     tilPort.editText!!.setText(PluginConfig.remoteSyncPort.toString())
                 }

                 MaterialAlertDialogBuilder(this)
                     .setView(view)
                     .setPositiveButton(android.R.string.ok) { _, _ ->
                         PluginConfig.isRemoteSyncEnabled = viewbaseBinding.sw.isChecked
                         PluginConfig.remoteSyncPort =
                             viewbaseBinding.tilPort.editText!!.text.toString().toInt()
                     }
                     .setNegativeButton(R.string.cancel, null)
                     .setNeutralButton(R.string.learn_more) { _, _ ->
                         val url = "https://github.com/jing332/tts-server-psc"
                         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         startActivity(intent)
                     }
                     .show()

             }*/

            R.id.menu_word_wrap -> {
                AppConfig.isCodeEditorWordWrapEnabled = !AppConfig.isCodeEditorWordWrapEnabled
                baseBinding.editor.isWordwrap = AppConfig.isCodeEditorWordWrapEnabled
            }

            R.id.menu_theme -> {
                val maps = linkedMapOf(
                    CodeEditorTheme.AUTO to "自动",
                    CodeEditorTheme.QUIET_LIGHT to "Quiet-Light",
                    CodeEditorTheme.SOLARIZED_DRAK to "Solarized-Dark",
                    CodeEditorTheme.DARCULA to "Darcula",
                    CodeEditorTheme.ABYSS to "Abyss"
                )
                val items = maps.values
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.theme)
                    .setSingleChoiceItems(
                        items.toTypedArray(),
                        AppConfig.codeEditorTheme
                    ) { dlg, which ->
                        AppConfig.codeEditorTheme = which
                        mEditorHelper.setTheme(which)
                        dlg.dismiss()
                    }
                    .setPositiveButton(R.string.cancel, null)
                    .show()
            }

            R.id.menu_set_sample_text -> {
                AppDialogs.displayInputDialog(
                    this,
                    getString(R.string.set_sample_text_param),
                    getString(R.string.sample_text),
                    PluginConfig.sampleText
                ) {
                    PluginConfig.sampleText = it
                }
            }

            R.id.menu_save_as_file -> saveAsFile()
            R.id.menu_save -> {
                kotlin.runCatching {
                    if (onSave()) {
                        setResult(
                            RESULT_OK,
                            Intent().apply { putExtra(KeyConst.KEY_DATA, getResultData()) })
                        finish()
                    }
                }.onFailure {
                    displayErrorDialog(it)
                }
            }

            R.id.menu_debug -> {
                onDebug()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    abstract fun getResultData(): Parcelable
    abstract fun getLogger(): Logger
    abstract fun onDebug()

    open fun displayDebugBottomSheet(logger: Logger = getLogger()) {
        val fragment =
            supportFragmentManager.findFragmentByTag("PluginLoggerBottomSheetFragment")
        if (fragment != null && fragment is LoggerBottomSheetFragment) {
            fragment.clearLog()
        } else {
            val bottomSheetFragment = LoggerBottomSheetFragment(logger)
            bottomSheetFragment.show(supportFragmentManager, "PluginLoggerBottomSheetFragment")
        }
    }
}