package com.github.jing332.tts_server_android.ui.systts.replace

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
import com.github.jing332.tts_server_android.databinding.SysttsReplaceEditActivityBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.adapter.initAccessibilityDelegate
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("DEPRECATION")
class ReplaceRuleEditActivity : BackActivity() {
    val binding: SysttsReplaceEditActivityBinding by lazy {
        SysttsReplaceEditActivityBinding.inflate(layoutInflater)
    }
    private val vm: ReplaceRuleEditViewModel by viewModels()

    private val pinyinList by lazy {
        "ā á ǎ à ê ē é ě è ī í ǐ ì ō ó ǒ ò ū ú ǔ ù ǖ ǘ ǚ ǜ".split(" ").toTypedArray()
    }

    var resultCode = RESULT_EDIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tilTest.initAccessibilityDelegate()
        binding.tilUrl.initAccessibilityDelegate()
        binding.tilPattern.initAccessibilityDelegate()
        binding.tilReplacement.initAccessibilityDelegate()

        binding.tilPattern.setEndIconOnClickListener { displayPinyinList() }
        binding.tilReplacement.setEndIconOnClickListener { displayPinyinList() }

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

        vm.liveData.observe(this) {
            binding.apply {
                etName.setText(it.name)
                etPattern.setText(it.pattern)
                etReplacement.setText(it.replacement)
                switchIsRegex.isChecked = it.isRegex
            }
        }

        intent.getParcelableExtra<ReplaceRule>(KeyConst.KEY_DATA).let {
            if (it == null) resultCode = RESULT_ADD
            vm.load(it)
        }
    }

    private fun displayPinyinList() {
        MaterialAlertDialogBuilder(this).setItems(
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
        }
            .setFadeAnim()
            .show()
    }

    private fun doTest() {
        val text = binding.etTestText.text.toString()
        val pattern = binding.etPattern.text.toString()
        val replacement = binding.etReplacement.text.toString()
        val isRegex = binding.switchIsRegex.isChecked

        val result =
            vm.test(text, pattern, replacement, isRegex).ifEmpty { getString(R.string.none) }
        binding.tvResult.text = result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_replace_rule_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                vm.liveData.value?.apply {
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
                val data = vm.liveData.value

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