package com.github.jing332.tts_server_android.ui.forwarder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.MsTtsForwarderFragmentBinding
import com.github.jing332.tts_server_android.ui.base.MenuHostFragment
import com.github.jing332.tts_server_android.utils.reduceDragSensitivity

abstract class AbsForwarderHostFragment : MenuHostFragment(R.layout.ms_tts_forwarder_fragment) {
    private val binding by viewBinding(MsTtsForwarderFragmentBinding::bind)
    private val vm: ForwarderHostViewModel by viewModels()

    abstract val isServiceRunning: Boolean
    abstract fun onSwitchChanged(isChecked: Boolean)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.switchStateLiveData.observe(viewLifecycleOwner) {
            onSwitchChanged(it)
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
                R.id.menu_serverLog -> binding.viewPager.setCurrentItem(0, true)
                R.id.menu_serverWeb -> binding.viewPager.setCurrentItem(1, true)
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