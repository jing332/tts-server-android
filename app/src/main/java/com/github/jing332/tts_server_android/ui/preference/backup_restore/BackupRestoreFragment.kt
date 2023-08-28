package com.github.jing332.tts_server_android.ui.preference.backup_restore

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.Attributes.colorOnBackground
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readBytes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackupRestoreFragment : PreferenceFragmentCompat() {
    companion object {
        private const val TAG = "BackupRestoreFragment"
    }

    private var hasRestorePref = false

    private val filePicker =
        registerForActivityResult(AppActivityResultContracts.filePickerActivity()) {
            it.first?.let { reqData ->
                if (reqData is FilePickerActivity.RequestSelectFile && it.second != null) {
                    restore(it.second!!.readBytes(requireContext()))
                }
            }
        }

    private val vm: BackupRestoreViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.backup_restore_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(getString(R.string.key_backup))?.apply {

            icon?.setTint(requireContext().colorOnBackground)
            setOnPreferenceClickListener {

                val items = Type.typeList.map { getString(it.nameStrId) }.toTypedArray()
                val checkedItems = items.map { true }.toBooleanArray()
                checkedItems[checkedItems.size - 1] = false
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(icon)
                    .setTitle(R.string.backup)
                    .setMultiChoiceItems(items, checkedItems) { dlg, which, isChecked ->
                        checkedItems[which] = isChecked
                        val type = Type.typeList[which]
                        // 如果勾选了插件变量，那么自动勾选插件; 反之，如果取消了插件，那么自动取消插件变量
                        if (isChecked && type is Type.PluginVars) {
                            (dlg as AlertDialog).listView.setItemChecked(
                                Type.typeList.indexOf(Type.Plugin),
                                true
                            )
                        } else if (!isChecked && type is Type.Plugin) {
                            val idx = Type.typeList.indexOf(Type.PluginVars)
                            checkedItems[idx] = false
                            (dlg as AlertDialog).listView.setItemChecked(idx, false)
                        }
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        lifecycleScope.launch {
                            runCatching {
                                val bytes = vm.backup(
                                    checkedItems.mapIndexed { index, b ->
                                        if (b) Type.typeList[index] else null
                                    }.filterNot { it == null }.map { it!! }
                                )
                                withMain {
                                    filePicker.launch(
                                        FilePickerActivity.RequestSaveFile(
                                            fileName = "ttsrv-backup.zip",
                                            fileMime = "application/zip",
                                            fileBytes = bytes
                                        )
                                    )
                                }
                            }.onFailure {
                                context.displayErrorDialog(it)
                            }
                        }
                    }
                    .show()

                true
            }
        }

        findPreference<Preference>(getString(R.string.key_restore))?.apply {
            icon?.setTint(requireContext().colorOnBackground)

            setOnPreferenceClickListener {
                filePicker.launch(FilePickerActivity.RequestSelectFile(listOf("application/zip")))
                true
            }
        }
    }

    private val waitDialog by lazy { WaitDialog(requireContext()) }


    fun restore(bytes: ByteArray) = lifecycleScope.launch(Dispatchers.Main) {
        waitDialog.show()
        val isRestart = try {
            withIO { vm.restore(bytes) }
        } catch (e: Exception) {
            requireContext().displayErrorDialog(e)
            false
        } finally {
            waitDialog.dismiss()
        }
        if (isRestart)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_finished)
                .setMessage(getString(R.string.restore_restart_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.restart) { _, _ ->
                    app.restart()
                }
                .show()
        else
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_finished)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }
}