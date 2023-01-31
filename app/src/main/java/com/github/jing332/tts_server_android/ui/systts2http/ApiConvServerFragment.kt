package com.github.jing332.tts_server_android.ui.systts2http

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.jing332.tts_server_android.databinding.ApiConvFragmentBinding

class ApiConvServerFragment : Fragment() {
    val binding: ApiConvFragmentBinding by lazy {
        ApiConvFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPort.text.toString().toInt()


    }

    private fun updateButtonState(isStarted: Boolean) {
        binding.btnStart.isEnabled = isStarted
        binding.btnClose.isEnabled = !isStarted
        binding.etPort.isEnabled = !isStarted
    }
}