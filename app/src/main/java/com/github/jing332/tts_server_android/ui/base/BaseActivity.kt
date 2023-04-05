package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.github.jing332.tts_server_android.util.inflateBinding
import com.github.jing332.tts_server_android.util.toast
import java.lang.reflect.ParameterizedType

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
