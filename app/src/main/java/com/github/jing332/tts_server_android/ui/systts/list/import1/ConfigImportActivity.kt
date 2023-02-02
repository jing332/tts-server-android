package com.github.jing332.tts_server_android.ui.systts.list.import1

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.jing332.tts_server_android.databinding.SysttsConfigImportActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity

class ConfigImportActivity : BackActivity() {
    val binding: SysttsConfigImportActivityBinding by lazy {
        SysttsConfigImportActivityBinding.inflate(layoutInflater)
    }

    private val vm: ConfigImportSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            setContentView(root)

            viewPager.adapter = ViewPagerAdapter(this@ConfigImportActivity)
            viewPager.offscreenPageLimit = 1
            viewPager.isUserInputEnabled = false

            vm.previewLiveData.observe(this@ConfigImportActivity) {
                viewPager.setCurrentItem(1, true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                backAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backAction() {
        if (binding.viewPager.currentItem == 0) finish()
        else binding.viewPager.setCurrentItem(0, true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    private val inputFragment = ConfigImportInputFragment()
    private val previewFragment = ConfigImportPreviewFragment()

    inner class ViewPagerAdapter(parent: FragmentActivity) : FragmentStateAdapter(parent) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) inputFragment else previewFragment
        }
    }

}