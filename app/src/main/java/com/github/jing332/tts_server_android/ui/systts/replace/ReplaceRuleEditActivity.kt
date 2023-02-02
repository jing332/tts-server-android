package com.github.jing332.tts_server_android.ui.systts.replace

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_EDIT
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.databinding.SysttsReplaceEditActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.max

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

        val data = intent.getParcelableExtra<ReplaceRule>(KeyConst.KEY_DATA)

        val groups = appDb.replaceRuleDao.allGroup
        val groupItems = groups.map { SpinnerItem(it.name, it) }
        binding.spinnerGroup.setAdapter(MaterialSpinnerAdapter(this, groupItems))
        binding.spinnerGroup.selectedPosition =
            max(0, groups.indexOfFirst { it.id == data?.groupId })
        binding.spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                data?.groupId = (groupItems[position].value as ReplaceRuleGroup).id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.tilPattern.setEndIconOnClickListener { displayPinyinList() }
        binding.tilReplacement.setEndIconOnClickListener { displayPinyinList() }

        binding.etPattern.addTextChangedListener {
            if (!binding.switchIsRegex.isChecked) {
                binding.etTestText.text.ifEmpty { binding.etTestText.text = binding.etPattern.text }
            }
            doTest()
        }

        binding.etReplacement.addTextChangedListener { doTest() }
        binding.etTestText.addTextChangedListener { doTest() }

        vm.liveData.observe(this) {
            binding.apply {
                switchIsRegex.isChecked = it.isRegex
                etName.setText(it.name)
                etPattern.setText(it.pattern)
                etReplacement.setText(it.replacement)
            }
        }

        data.let {
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
        menuInflater.inflate(R.menu.systts_replace_rule_edit, menu)
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