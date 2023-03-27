package com.github.jing332.tts_server_android.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ExoPlayerActivityBinding

class ExoPlayerActivity : AppCompatActivity() {
    private val binding by lazy { ExoPlayerActivityBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}