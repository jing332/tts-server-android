package com.github.jing332.tts_server_android.ui.systts.base

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.SysttsBaseScriptEditorActivityBinding
import com.github.jing332.tts_server_android.databinding.SysttsScriptSyncSettingsBinding
import com.github.jing332.tts_server_android.help.config.PluginConfig
import com.github.jing332.tts_server_android.help.config.ScriptEditorConfig
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.CodeEditorHelper
import com.github.jing332.tts_server_android.util.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseScriptEditorActivity : BackActivity() {
    private lateinit var fileSaver: ActivityResultLauncher<FilePickerActivity.IRequestData>
    private lateinit var mEditorHelper: CodeEditorHelper
    private val baseBinding by lazy { SysttsBaseScriptEditorActivityBinding.inflate(layoutInflater) }
    private val vm: BaseScriptEditorViewModel by viewModels()

    private var savedData: ByteArray? = null

    val editor: CodeEditor by lazy { baseBinding.editor }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(baseBinding.root)

        fileSaver = registerForActivityResult(AppActivityResultContracts.filePickerActivity()) {

        }

        mEditorHelper = CodeEditorHelper(this, baseBinding.editor)
        mEditorHelper.initEditor()
        mEditorHelper.setTheme(ScriptEditorConfig.codeEditorTheme)

        if (ScriptEditorConfig.isRemoteSyncEnabled) {
            lifecycleScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    vm.startSyncServer(
                        ScriptEditorConfig.remoteSyncPort,
                        onPush = {
                            baseBinding.editor.setText(it)
                            updateCode(it)
                            onScriptSyncPush()
                        },
                        onPull = { baseBinding.editor.text.toString() },
                        onDebug = { onDebug() },
                        onAction = { name, body -> onScriptSyncAction(name, body) }
                    )
                }.onFailure {
                    displayErrorDialog(it)
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.systts_base_script_editor, menu)
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.menu_word_wrap)?.isChecked =
                ScriptEditorConfig.isCodeEditorWordWrapEnabled
            findItem(R.id.menu_remote_sync)?.isChecked = ScriptEditorConfig.isRemoteSyncEnabled
        }
        return super.onPrepareOptionsMenu(menu)
    }

    abstract fun onScriptSyncAction(name: String, body: ByteArray?)
    abstract fun onScriptSyncPush()

    /**
     * 保存和Debug前调用
     * @param code 编辑器View的代码文本
     */
    abstract fun updateCode(code: String)
    abstract fun clearCacheFile(): Boolean

    /**
     * @return 文件名
     */
    abstract fun onGetSaveFileName(): String

    /**
     * 保存按钮
     */
    abstract fun onSave(): Parcelable?

    private fun saveAsFile() {
        savedData = editor.text.toString().toByteArray()
        fileSaver.launch(
            FilePickerActivity.RequestSaveFile(
                onGetSaveFileName(), "text/javascript",
                editor.text.toString().toByteArray()
            )
        )
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear_cache -> {
                if (clearCacheFile()) longToast(R.string.cleared)
            }

            R.id.menu_remote_sync -> {
                val view = FrameLayout(this)
                val syncBinding =
                    SysttsScriptSyncSettingsBinding.inflate(layoutInflater, view, true)
                syncBinding.apply {
                    tvTip.text =
                        Html.fromHtml(getString(R.string.script_sync_service_tip))

                    sw.isChecked = ScriptEditorConfig.isRemoteSyncEnabled
                    tilPort.editText!!.setText(ScriptEditorConfig.remoteSyncPort.toString())
                }

                MaterialAlertDialogBuilder(this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        ScriptEditorConfig.isRemoteSyncEnabled = syncBinding.sw.isChecked
                        ScriptEditorConfig.remoteSyncPort =
                            syncBinding.tilPort.editText!!.text.toString().toInt()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.learn_more) { _, _ ->
                        val url = "https://github.com/jing332/tts-server-psc"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                    .show()

            }

            R.id.menu_word_wrap -> {
                ScriptEditorConfig.isCodeEditorWordWrapEnabled =
                    !ScriptEditorConfig.isCodeEditorWordWrapEnabled
                baseBinding.editor.isWordwrap = ScriptEditorConfig.isCodeEditorWordWrapEnabled
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
                        ScriptEditorConfig.codeEditorTheme
                    ) { dlg, which ->
                        ScriptEditorConfig.codeEditorTheme = which
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
                updateCode(baseBinding.editor.text.toString())
                kotlin.runCatching {
                    onSave()?.let { data ->
                        setResult(
                            RESULT_OK,
                            Intent().apply { putExtra(KeyConst.KEY_DATA, data) })
                        finish()
                    }
                }.onFailure {
                    displayErrorDialog(it)
                }
            }

            R.id.menu_debug -> {
                updateCode(baseBinding.editor.text.toString())
                onDebug()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

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