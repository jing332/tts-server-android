package com.github.jing332.tts_server_android.ui.sys_forwarder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysTtsForwarderFragmentBinding
import com.github.jing332.tts_server_android.util.reduceDragSensitivity
import com.github.jing332.tts_server_android.util.toast

class SysTtsForwarderFragment : Fragment() {
    val binding: SysTtsForwarderFragmentBinding by lazy {
        SysTtsForwarderFragmentBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vp.reduceDragSensitivity(8)
        binding.vp.isSaveEnabled = false
        binding.vp.adapter = FragmentAdapter(this)
        binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bnv.menu.getItem(position).isChecked = true
            }
        })
        binding.bnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_serverLog -> binding.vp.setCurrentItem(0, true)
                R.id.menu_serverWeb -> {
                    binding.vp.setCurrentItem(0, true)
                    toast("😛暂不支持")
                }
            }
            true
        }
    }

    private val logPage = SysTtsServerLogPage()

    inner class FragmentAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        private val fragmentList = listOf(logPage)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }


}