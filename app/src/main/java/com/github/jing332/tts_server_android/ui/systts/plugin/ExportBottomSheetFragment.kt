package com.github.jing332.tts_server_android.ui.systts.plugin

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class ExportBottomSheetFragment : ConfigExportBottomSheetFragment() {
    private lateinit var pluginList: Array<Plugin>
    private lateinit var includeVarsCheckBox: CheckBox
    private var name: String? = null

    companion object {
        private const val KEY_PLUGIN_LIST = "pluginList"
        private const val KEY_NAME = "name"

        fun newInstance(name: String, list: List<Plugin>? = null): ExportBottomSheetFragment {
            return ExportBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArray(KEY_PLUGIN_LIST, list?.toTypedArray())
                    putString(KEY_NAME, name)
                }
            }
        }
    }

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = arguments?.getString(KEY_NAME) ?: ""
        val plugins = arguments?.getParcelableArray(KEY_PLUGIN_LIST) as? Array<Plugin>
        lifecycleScope.launch(Dispatchers.Main) {
            if (plugins == null) {
                withIO { pluginList = appDb.pluginDao.allEnabled.toTypedArray() }
            } else
                pluginList = plugins

            updateConfig()
        }
    }

    private fun updateConfig(isIncludeVars: Boolean = includeVarsCheckBox.isChecked) {
        val cfg = if (isIncludeVars) {
            AppConst.jsonBuilder.encodeToString(pluginList)
        } else {
            AppConst.jsonBuilder.encodeToString(pluginList.map { it.copy(userVars = mutableMapOf()) })
        }
        setConfigString(cfg)
    }

    override fun onCreateContainerView(inflater: LayoutInflater): View {
        val linearLayout = LinearLayout(requireContext())
        linearLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearLayout.gravity = Gravity.CENTER_HORIZONTAL
        includeVarsCheckBox = CheckBox(requireContext())

        includeVarsCheckBox.setText(R.string.export_vars)
        includeVarsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateConfig(isChecked)
        }
        includeVarsCheckBox.isChecked = false

        linearLayout.addView(includeVarsCheckBox)
        return linearLayout
    }


}