package com.github.jing332.tts_server_android.ui.systts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsHostFragmentBinding
import com.github.jing332.tts_server_android.ui.systts.list.SysTtsListFragment
import com.github.jing332.tts_server_android.utils.reduceDragSensitivity


class SysTtsHostFragment : Fragment(R.layout.systts_host_fragment) {
    private val binding by viewBinding(SysttsHostFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.isSaveEnabled = false
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
                R.id.navbar_config -> binding.viewPager.setCurrentItem(0, true)
                R.id.navbar_log -> binding.viewPager.setCurrentItem(1, true)
            }
            true
        }

    }

    inner class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val fragmentList = listOf(SysTtsListFragment(), SysTtsLogPageFragment())
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}