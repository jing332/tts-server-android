package com.github.jing332.tts_server_android.ui.systts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsFragmentBinding
import com.github.jing332.tts_server_android.ui.systts.list.SysTtsListFragment
import com.github.jing332.tts_server_android.util.reduceDragSensitivity


class SysTtsFragment : Fragment() {
    private val binding: SysttsFragmentBinding by lazy {
        SysttsFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    val listFragment = SysTtsListFragment()
    val logFragment = SysTtsLogFragment()

    inner class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val fragmentList = listOf(listFragment, logFragment)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}