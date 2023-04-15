package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsBaseEditActivityBinding
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.InputStream

open class BaseTtsEditActivity<T : ITextToSpeechEngine>(private val factory: () -> T) :
    BackActivity() {
    companion object {
        const val KEY_DATA = "KEY_DATA"
        const val KEY_BASIC_VISIBLE = "KEY_BASIC_VISIBLE"
    }

    private var mAudioPlayer: AudioPlayer? = null

    suspend fun playAudio(audio: ByteArray) {
        mAudioPlayer = mAudioPlayer ?: AudioPlayer(this, lifecycleScope)
        mAudioPlayer?.play(audio)
    }

    suspend fun playAudio(inputStream: InputStream) {
        mAudioPlayer = mAudioPlayer ?: AudioPlayer(this, lifecycleScope)
        mAudioPlayer?.play(inputStream)
    }

    fun stopPlay() {
        mAudioPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioPlayer?.release()
    }

    open fun onSave() {
        basicEditView.saveData()
        setResult(RESULT_OK, Intent().apply { putExtra(KEY_DATA, systemTts) })
        finish()
    }

    open fun onTest(text: String) {}

    private val binding: SysttsBaseEditActivityBinding by lazy {
        SysttsBaseEditActivityBinding.inflate(layoutInflater)
    }

    protected var testInputLayout: TextInputLayout? = null
    protected val basicEditView: BasicInfoEditView by lazy { binding.basicEdit }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(binding.root)

        val visible = intent.getBooleanExtra(KEY_BASIC_VISIBLE, true)
        binding.basicEdit.visibility = if (visible) View.VISIBLE else View.GONE
        binding.divider.visibility = binding.basicEdit.visibility

        binding.basicEdit.setData(systemTts, lifecycleScope)
    }

    fun setEditContentView(view: View?, testTil: TextInputLayout? = null) {
        binding.content.removeAllViews()
        binding.content.addView(view)
        if (basicEditView.liteModeEnabled) return

        this.testInputLayout = testTil
        if (this.testInputLayout == null) {
            this.testInputLayout = binding.testLayout.tilTest
            binding.testLayout.root.visibility = View.VISIBLE
        }

        testInputLayout?.editText?.apply {
            setText(AppConfig.testSampleText)

            testInputLayout?.setEndIconOnClickListener {
                val textStr = text.toString()
                if (textStr.isBlank()) setText(AppConfig.testSampleText)
                onTest(textStr)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppConfig.testSampleText = testInputLayout?.editText?.text?.toString() ?: ""
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
        menuInflater.inflate(R.menu.systts_config_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
            onSave()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}