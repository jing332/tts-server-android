package com.github.jing332.tts_server_android.ui.systts.plugin

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.databinding.SysttsPluginVarsSettingsBottomSheetBinding
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.setMarginMatchParent
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PluginVarsSettingsBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        private const val KEY_TITLE = "TITLE_STRING_RES"
        private const val KEY_PLUGIN = "PLUGIN"

        fun newInstance(title: CharSequence, plugin: Plugin) =
            PluginVarsSettingsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putCharSequence(KEY_TITLE, title)
                    putParcelable(KEY_PLUGIN, plugin)
                }
            }
    }

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

        binding.tvTitle.text =
            arguments?.getCharSequence(KEY_TITLE) ?: getString(R.string.plugin_vars)
        plugin = arguments?.getParcelable(KEY_PLUGIN)

        if (plugin?.defVars?.isEmpty() == true) {
            toast(R.string.plugin_not_define_vars)
            dismiss()
            return
        }

        plugin!!.defVars.forEach {
            val key = it.key

            val input = MaterialTextInput(requireContext())
            input.hint = it.value["label"]
            input.placeholderText = it.value["hint"]
            input.editText?.addTextChangedListener { et ->
                if (et.isNullOrBlank()) {
                    plugin!!.mutableUserVars.remove(key)
                } else
                    plugin!!.mutableUserVars[key] = et.toString()
            }
            input.editText?.setText(plugin!!.userVars.getOrDefault(key, ""))

            binding.labelVars.addView(
                input,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            (input.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 4.dp
        }
    }
}