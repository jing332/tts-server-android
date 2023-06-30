package com.github.jing332.tts_server_android.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityBackBinding
import com.github.jing332.tts_server_android.ui.view.ThemeExtensions.initAppTheme

/**
 * 带behavior
 */
open class BackActivity : AppCompatActivity() {
    protected val rootBinding: ActivityBackBinding by lazy {
        ActivityBackBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initAppTheme()
        super.onCreate(savedInstanceState)
        super.setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun setContentView(view: View?) {
        rootBinding.content.addView(view)
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