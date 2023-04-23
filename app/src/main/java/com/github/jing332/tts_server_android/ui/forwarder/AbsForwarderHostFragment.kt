package com.github.jing332.tts_server_android.ui.forwarder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ForwarderHostFragmentBinding
import com.github.jing332.tts_server_android.ui.base.MenuHostFragment
import com.github.jing332.tts_server_android.utils.observeNoSticky
import com.github.jing332.tts_server_android.utils.reduceDragSensitivity

abstract class AbsForwarderHostFragment : MenuHostFragment(R.layout.forwarder_host_fragment) {
    private val binding by viewBinding(ForwarderHostFragmentBinding::bind)
    private val vm: ForwarderHostViewModel by viewModels()

    abstract val isServiceRunning: Boolean
    abstract fun onSwitchChanged(isChecked: Boolean)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.switchStateLiveData.observeNoSticky(viewLifecycleOwner) {
            onSwitchChanged(it)
        }

        vm.viewPageIndexLiveData.observeNoSticky(viewLifecycleOwner) {
            binding.viewPager.setCurrentItem(it, true)
        }

        binding.viewPager.reduceDragSensitivity(8)
        binding.viewPager.adapter = FragmentAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bnv.menu.getItem(position).isChecked = true
            }
        })
        binding.bnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_serverLog -> vm.viewPageIndexLiveData.value = 0
                R.id.menu_serverWeb -> vm.viewPageIndexLiveData.value = 1
            }
            true
        }
    }

    abstract val homePageFragment: AbsForwarderHomePageFragment
    abstract val webPageFragment: AbsForwarderWebPageFragment

    inner class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val logPage by lazy { homePageFragment }
        private val webPage by lazy { webPageFragment }

        private val fragmentList = listOf(logPage, webPage)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}