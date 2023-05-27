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
import com.github.jing332.tts_server_android.model.rhino.direct_link_upload.DirectUploadEngine
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ConfigExportBottomSheetFragment(
    private val onGetConfig: (() -> String)? = null,
    private val onGetName: (() -> String)? = null
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ConfigExportBottomSheetFragment"
    }

    private lateinit var fileSaver: ActivityResultLauncher<FilePickerActivity.IRequestData>
    private val binding by lazy { SysttsConfigExportBottomSheetBinding.inflate(layoutInflater) }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        fileSaver = registerForActivityResult(AppActivityResultContracts.filePickerActivity()) {

        }
    }

    open fun onCreateContainerView(inflater: LayoutInflater): View? = null
    open fun getConfig(): String = ""
    open fun getFileName(): String = ""

    open fun onUpdateConfig() {
        internalGetConfig().let {
            if (it.isNotBlank())
                binding.tvConfig.text = it
        }
    }

    fun setConfigString(config: String) {
        binding.tvConfig.text = config
    }

    private fun setContainerView(view: View) {
        binding.container.removeAllViews()
        binding.container.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onCreateContainerView(layoutInflater)?.let {
            setContainerView(it)
        }

        (requireView().parent as ViewGroup).setMarginMatchParent()
        onUpdateConfig()

        binding.btnCopy.clickWithThrottle {
            ClipboardUtils.copyText(binding.tvConfig.text.toString())
            toast(R.string.copied)
        }

        binding.btnSave.clickWithThrottle {
            fileSaver.launch(
                FilePickerActivity.RequestSaveFile(
                    fileName = internalGetName(),
                    fileBytes = binding.tvConfig.text.toString().toByteArray()
                )
            )
        }

        binding.btnUpload.clickWithThrottle {
            upload(binding.tvConfig.text.toString())
        }
    }

    private fun internalGetName(): String {
        return onGetName?.invoke() ?: getFileName()
    }

    private fun internalGetConfig(): String {
        return onGetConfig?.invoke() ?: getConfig()
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