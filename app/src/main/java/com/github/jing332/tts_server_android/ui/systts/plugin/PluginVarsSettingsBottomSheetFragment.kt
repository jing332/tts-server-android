package com.github.jing332.tts_server_android.ui.systts.plugin

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.databinding.SysttsPluginVarsSettingsBottomSheetBinding
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput
import com.github.jing332.tts_server_android.util.setMarginMatchParent
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PluginVarsSettingsBottomSheetFragment : BottomSheetDialogFragment() {
    private var plugin: Plugin? = null
    private val binding by lazy { SysttsPluginVarsSettingsBottomSheetBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    @Suppress("DEPRECATION")
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        plugin?.let {
            appDb.pluginDao.update(it)
        }
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.parent as ViewGroup).setMarginMatchParent()

        plugin = arguments?.getParcelable<Plugin>(KeyConst.KEY_DATA)?.run { this as Plugin }
        if (plugin == null) {
            dismiss()
            return
        }

        if (plugin?.defVars.isNullOrEmpty()) {
            toast("此插件未定义变量")
            dismiss()
            return
        }

        plugin!!.defVars.forEach {
            val key = it.key

            val input = MaterialTextInput(requireContext())
            input.hint = it.value["label"]
            input.placeholderText = it.value["hint"]
            input.editText?.setText(plugin!!.userVars.getOrDefault(key, ""))
            input.editText?.addTextChangedListener { et ->
                plugin!!.userVars[key] = et.toString()
            }

            binding.labelVars.addView(
                input,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

    }
}