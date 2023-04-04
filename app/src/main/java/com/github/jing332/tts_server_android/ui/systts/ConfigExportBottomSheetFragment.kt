package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsConfigExportBottomSheetBinding
import com.github.jing332.tts_server_android.model.rhino.directupload.DirectUploadEngine
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.util.ClipboardUtils
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigExportBottomSheetFragment(
    private val onGetConfig: () -> String,
    private val onGetName: () -> String
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ConfigExportBottomSheetFragment"
    }

    private val binding by lazy { SysttsConfigExportBottomSheetBinding.inflate(layoutInflater) }
    private lateinit var fileSaver: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fileSaver = FileUtils.registerResultCreateDocument(this, "application/json") {
            binding.tvConfig.text.toString().toByteArray()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sheetContainer = requireView().parent as ViewGroup
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val config = onGetConfig.invoke()
        binding.tvConfig.text = config

        binding.btnCopy.clickWithThrottle {
            ClipboardUtils.copyText(binding.tvConfig.text.toString())
            toast(R.string.copied)
        }

        binding.btnSave.clickWithThrottle {
            fileSaver.launch(onGetName.invoke())
        }

        binding.btnUpload.clickWithThrottle {
            upload(binding.tvConfig.text.toString())
        }
    }

    private val uploadEngine by lazy { DirectUploadEngine(context = requireContext()) }
    private val waitDialog by lazy { WaitDialog(requireContext()) }

    private fun upload(config: String) {
        val list = try {
            uploadEngine.obtainFunctionList()
        } catch (t: Throwable) {
            requireContext().displayErrorDialog(t)
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_an_upload_target)
            .setItems(list.map { it.funcName }.toTypedArray()) { dlg, which ->
                dlg.dismiss()
                val selected = list[which]
                waitDialog.show()
                lifecycleScope.launch(Dispatchers.Main) {
                    kotlin.runCatching {
                        val url = withIO { selected.invoke(config) }
                        if (url.isNullOrBlank())
                            longToast(R.string.warn_returned_url_is_empty)
                        else {
                            ClipboardUtils.copyText(url)
                            longToast(R.string.copied_url)
                        }

                    }.onFailure {
                        context?.displayErrorDialog(it)
                    }

                    waitDialog.dismiss()
                }
            }
            .setPositiveButton(R.string.cancel, null)
            .setNeutralButton(R.string.direct_link_settings) { _, _ ->
                requireContext().startActivity(
                    Intent(
                        requireContext(), DirectUploadSettingsActivity::class.java
                    )
                )
            }
            .show()
    }

}