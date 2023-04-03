package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), IBinding<VB> {
    override lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::binding.isInitialized) {
            kotlin.runCatching {
                binding = inflateBinding(layoutInflater)
            }.onFailure {
                it.printStackTrace()
            }
        }
        setContentView(binding.root)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        internal fun <T : ViewBinding> Any.inflateBinding(inflater: LayoutInflater): T {
            return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
                .filterIsInstance<Class<T>>()
                .first()
                .getDeclaredMethod("inflate", LayoutInflater::class.java)
                .also { it.isAccessible = true }
                .invoke(null, inflater) as T
        }
    }
}
