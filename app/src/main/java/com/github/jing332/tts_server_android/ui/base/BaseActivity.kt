package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.github.jing332.tts_server_android.utils.inflateBinding
import com.github.jing332.tts_server_android.utils.toast

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), IBinding<VB> {
    override lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::binding.isInitialized) {
            kotlin.runCatching {
                binding = inflateBinding(layoutInflater)
            }.onFailure {
                toast("布局实例化失败！")
            }
        }
        setContentView(binding.root)
    }
}
