package com.github.jing332.tts_server_android.ui.systts.list.import1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsConfigImportPreviewFragmentBinding
import com.github.jing332.tts_server_android.util.clickWithThrottle

class ConfigImportPreviewFragment : Fragment() {
    val binding: SysttsConfigImportPreviewFragmentBinding by lazy {
        SysttsConfigImportPreviewFragmentBinding.inflate(layoutInflater, null, false)
    }

    private val vm: ConfigImportSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    private lateinit var brv: BindingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        brv = binding.recyclerView.linear().setup {
            addType<RvModelPreview>(R.layout.systts_config_import_preview_item)
        }

        vm.previewLiveData.observe(viewLifecycleOwner) { liveData ->
            val list = mutableListOf<Pair<SystemTtsGroup, SystemTts>>()
            liveData.forEach {
                it.list.forEach { tts -> list.add(Pair(it.group, tts)) }
            }
            brv.models = list.map {
                RvModelPreview(
                    name = it.second.displayName.toString(),
                    groupName = it.first.name,
                    data = it
                )
            }

        }

        binding.btnOk.clickWithThrottle {
            val models = brv.models!!.map { it as RvModelPreview }.filter { it.isChecked }
            val groups = models.map { it.data.first }.distinctBy { it.id }

            val groupWithTtsItems =
                groups.map { GroupWithTtsItem(group = it, list = mutableListOf()) }
            models.forEach { rvModel ->
                val tts = rvModel.data.second
                groupWithTtsItems.forEach {
                    if (it.group.id == tts.groupId)
                        (it.list as MutableList).add(tts)
                }
            }

            doImport(groupWithTtsItems)
            requireActivity().finish()
        }
    }


    private fun doImport(list: List<GroupWithTtsItem>) {
        list.forEach {
            appDb.systemTtsDao.insertGroup(it.group)
            appDb.systemTtsDao.insertTts(*it.list.toTypedArray())
        }
    }

}