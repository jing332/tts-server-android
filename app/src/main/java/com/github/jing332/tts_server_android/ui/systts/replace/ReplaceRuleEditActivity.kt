package com.github.jing332.tts_server_android.ui.systts.replace

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_EDIT
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.databinding.ActivityReplaceRuleEditBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity

@Suppress("DEPRECATION")
class ReplaceRuleEditActivity : BackActivity() {
    val binding by lazy { ActivityReplaceRuleEditBinding.inflate(layoutInflater) }
    val viewModel: ReplaceRuleEditViewModel by viewModels()

    private val pinyinList by lazy {
        "ā á ǎ à ê ē é ě è ī í ǐ ì ō ó ǒ ò ū ú ǔ ù ǖ ǘ ǚ ǜ".split(" ").toTypedArray()
    }

    var resultCode = RESULT_EDIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnPinyinList.setOnClickListener {
            AlertDialog.Builder(this).setItems(
                pinyinList
            ) { _, which ->
                val pinyin = pinyinList[which]
                if (binding.etPattern.hasFocus()) {
                    binding.etPattern.apply {
                        text.insert(selectionStart, pinyin)
                    }
                } else if (binding.etReplacement.hasFocus()) {
                    binding.etReplacement.apply {
                        text.insert(selectionStart, pinyin)
                    }
                } else if (binding.etName.hasFocus()) {
                    binding.etName.apply {
                        text.insert(selectionStart, pinyin)
                    }
                }
            }.create().apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }.show()
        }

        binding.etPattern.addTextChangedListener {
            if (binding.etTestText.text.isEmpty()) {
                binding.etTestText.text = it
            }
        }

        binding.etReplacement.addTextChangedListener {
            doTest()
        }

        binding.etTestText.addTextChangedListener {
            doTest()
        }

        binding.btnTest.setOnClickListener {
            doTest()
        }

        viewModel.liveData.observe(this) {
            binding.apply {
                etName.setText(it.name)
                etPattern.setText(it.pattern)
                etReplacement.setText(it.replacement)
                switchIsRegex.isChecked = it.isRegex
            }
        }

        intent.getParcelableExtra<ReplaceRule>(KeyConst.KEY_DATA).let {
            if (it == null) resultCode = RESULT_ADD
            viewModel.load(it)
        }
    }

    private fun doTest() {
        val text = binding.etTestText.text.toString()
        val pattern = binding.etPattern.text.toString()
        val replacement = binding.etReplacement.text.toString()
        val isRegex = binding.switchIsRegex.isChecked

        val result =
            viewModel.test(text, pattern, replacement, isRegex).ifEmpty { getString(R.string.none) }
        binding.tvResult.text = result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_replace_rule_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                viewModel.liveData.value?.apply {
                    pattern = binding.etPattern.text.toString()
                    if (pattern.isEmpty()) {
                        binding.etPattern.error = getString(R.string.cannot_empty)
                        binding.etPattern.requestFocus()

                        return true
                    }

                    name = binding.etName.text.toString().ifEmpty { pattern }
                    replacement = binding.etReplacement.text.toString()
                    isRegex = binding.switchIsRegex.isChecked
                }
                val data = viewModel.liveData.value

                val intent = Intent()
                intent.putExtra(
                    KeyConst.KEY_DATA,
                    data
                )
                setResult(resultCode, intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

}