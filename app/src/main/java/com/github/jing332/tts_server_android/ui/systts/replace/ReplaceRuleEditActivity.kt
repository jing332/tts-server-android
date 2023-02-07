package com.github.jing332.tts_server_android.ui.systts.replace

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.databinding.SysttsReplaceEditActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.max

@Suppress("DEPRECATION")
class ReplaceRuleEditActivity : BackActivity() {
    val binding: SysttsReplaceEditActivityBinding by lazy {
        SysttsReplaceEditActivityBinding.inflate(layoutInflater)
    }
    private val vm: ReplaceRuleEditViewModel by viewModels()

    private val pinyinList by lazy {
        arrayOf(
            "ā- a1",
            "á - a2",
            "ǎ - a3",
            "à - a4",
            "ê - e?",
            "ē - e1",
            "é - e2",
            "ě - e3",
            "è - e4",
            "ī - i1",
            "í - i2",
            "ǐ - i3",
            "ì - i4",
            "ō - o1",
            "ó - o2",
            "ǒ - o3",
            "ò - o4",
            "ū - u1",
            "ú - u2",
            "ǔ - u3",
            "ù - u4",
            "ǖ - v1",
            "ǘ - u2",
            "ǚ - u3",
            "ǜ - u4"
        )
    }
    private lateinit var data: ReplaceRule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        data = intent.getParcelableExtra(KeyConst.KEY_DATA) ?: ReplaceRule()

        val groups = appDb.replaceRuleDao.allGroup
        val groupItems = groups.map { SpinnerItem(it.name, it) }
        binding.spinnerGroup.setAdapter(MaterialSpinnerAdapter(this, groupItems))
        binding.spinnerGroup.selectedPosition =
            max(0, groups.indexOfFirst { it.id == data.groupId })
        binding.spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                data.groupId = (groupItems[position].value as ReplaceRuleGroup).id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.tilPattern.setEndIconOnClickListener { displayPinyinDialog() }
        binding.tilReplacement.setEndIconOnClickListener { displayPinyinDialog() }

        binding.etPattern.addTextChangedListener {
            if (!binding.switchIsRegex.isChecked) {
                binding.etTestText.text.ifEmpty { binding.etTestText.text = binding.etPattern.text }
            }
            doTest()
        }

        binding.etReplacement.addTextChangedListener { doTest() }
        binding.etTestText.addTextChangedListener { doTest() }

        binding.apply {
            switchIsRegex.isChecked = data.isRegex
            etName.setText(data.name)
            etPattern.setText(data.pattern)
            etReplacement.setText(data.replacement)
        }
    }

    private fun displayPinyinDialog() {
        val chipGroup = ChipGroup(this).apply {
            setChipSpacingHorizontalResource(R.dimen.chip)
            setPadding(32)
        }
        val dlg = MaterialAlertDialogBuilder(this)
            .setView(chipGroup)
            .show()
        pinyinList.forEach { py ->
            val chipItem = Chip(this)
            chipItem.text = py
            chipItem.setTypeface(null, Typeface.BOLD)
            chipItem.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge)
            chipGroup.addView(chipItem)
            chipItem.setOnClickListener {
                dlg.dismiss()
                val char = py[0].toString()
                if (binding.etPattern.hasFocus()) {
                    binding.etPattern.apply {
                        text.insert(selectionStart, char)
                    }
                } else if (binding.etReplacement.hasFocus()) {
                    binding.etReplacement.apply {
                        text.insert(selectionStart, char)
                    }
                }
            }
        }
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
                data.apply {
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
                val intent = Intent()
                intent.putExtra(KeyConst.KEY_DATA, data)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

}