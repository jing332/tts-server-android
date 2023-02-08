package com.github.jing332.tts_server_android.ui.systts.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListSimpleGroupFragmentBinding
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ListPageFragment : Fragment() {
    companion object {
        private const val ARG_RA_TARGET = "ARG_RA_TARGET"

        fun newInstance(raTarget: Int = -1): ListPageFragment {
            return ListPageFragment().apply {
                arguments = Bundle().apply { putInt(ARG_RA_TARGET, raTarget) }
            }
        }
    }

    private val binding: SysttsListSimpleGroupFragmentBinding by lazy {
        SysttsListSimpleGroupFragmentBinding.inflate(layoutInflater)
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
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        val brv = binding.rv.linear().setup {
            addType<SystemTts>(R.layout.systts_list_item)
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
