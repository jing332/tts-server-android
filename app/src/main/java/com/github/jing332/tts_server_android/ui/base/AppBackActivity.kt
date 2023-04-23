package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.AppBackActivityBinding

/**
 * 一个自定义的Activity，用于实现返回按钮
 *
 * ViewBinding的使用: by viewBinding(xxxBinding::bind){contentView}
 *@param layoutId 布局ID
 */
open class AppBackActivity(@LayoutRes val layoutId: Int) :
    AppCompatActivity(R.layout.app_back_activity) {
    private val binding by viewBinding(AppBackActivityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TtsServer_NoActionBar)
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val contentBinding = layoutInflater.inflate(layoutId, null, false)
        binding.container.addView(contentBinding.rootView)
    }

    /**
     * 实际内容View，用于ViewBinding
     */
    protected val contentView: View
        get() = binding.container.getChildAt(0)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}