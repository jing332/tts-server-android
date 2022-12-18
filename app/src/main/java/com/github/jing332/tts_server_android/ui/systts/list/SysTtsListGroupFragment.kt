package com.github.jing332.tts_server_android.ui.systts.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListGroupFragmentBinding
import com.github.jing332.tts_server_android.databinding.SysttsListItem2Binding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@Suppress("DEPRECATION")
class SysTtsListGroupFragment : Fragment() {
    companion object {
        private const val ARG_RA_TARGET = "ARG_RA_TARGET"

        fun newInstance(raTarget: Int = -1): SysTtsListGroupFragment {
            return SysTtsListGroupFragment().apply {
                arguments = Bundle().apply { putInt(ARG_RA_TARGET, raTarget) }
            }
        }
    }

    private val binding: SysttsListGroupFragmentBinding by lazy {
        SysttsListGroupFragmentBinding.inflate(layoutInflater)
    }

    private val itemHelper = SysTtsListItemHelper(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        val brv = binding.rv.linear().setup {
            addType<SystemTts>(R.layout.systts_list_item2)
            onCreate {
                itemHelper.init(this@setup, this)
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SystemTts).id == (newItem as SystemTts).id
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }
        }

        val raTarget = arguments?.getInt(ARG_RA_TARGET)
        lifecycleScope.launch(Dispatchers.IO) {
            appDb.systemTtsDao.flowAllTts.conflate().collect { list ->
                val filteredList =
                    if (raTarget == -1) list else list.filter { it.readAloudTarget == raTarget }
                val handledList = filteredList.sortedBy { it.readAloudTarget }

                if (brv.models == null) withMain { brv.models = handledList }
                else brv.setDifferModels(handledList)
            }
        }
    }
}
