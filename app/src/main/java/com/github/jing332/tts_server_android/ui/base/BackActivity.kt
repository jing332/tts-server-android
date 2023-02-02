package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityBackBinding

open class BackActivity : AppCompatActivity() {
    private val binding: ActivityBackBinding by lazy { ActivityBackBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TtsServer_NoActionBar)
        super.onCreate(savedInstanceState)
        super.setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun setContentView(view: View?) {
        binding.content.addView(view)
    }

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