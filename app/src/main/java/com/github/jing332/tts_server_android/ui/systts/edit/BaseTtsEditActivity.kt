package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.ui.custom.BackActivity

open class BaseTtsEditActivity<T : BaseTTS>(private val factory: () -> T) : BackActivity() {
    companion object {
        const val KEY_DATA = "KEY_DATA"
    }

    open fun onSave() {
        setResult(RESULT_OK, Intent().apply { putExtra(KEY_DATA, systemTts) })
        finish()
    }



    private var mData: SystemTts? = null


    @Suppress("DEPRECATION")
    val systemTts: SystemTts
        get() {
            mData?.let { return it }
            mData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(KEY_DATA, SystemTts::class.java)
            } else {
                intent.getParcelableExtra(KEY_DATA)
            }

            mData = mData ?: SystemTts(tts = factory())
            return mData!!
        }

    @Suppress("UNCHECKED_CAST")
    val tts: T
        get() {
            return systemTts.tts as T
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_systts_config_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
            onSave()
        }

        return super.onOptionsItemSelected(item)
    }
}