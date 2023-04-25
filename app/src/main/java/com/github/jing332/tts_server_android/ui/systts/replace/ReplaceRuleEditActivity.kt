package com.github.jing332.tts_server_android.ui.systts.replace

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.databinding.SysttsReplaceEditActivityBinding
import com.github.jing332.tts_server_android.ui.base.AppBackActivity
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.utils.dp
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.max


@Suppress("DEPRECATION")
class ReplaceRuleEditActivity : AppBackActivity(R.layout.systts_replace_edit_activity) {
    private val binding by viewBinding(SysttsReplaceEditActivityBinding::bind)
    private val vm: ReplaceRuleEditViewModel by viewModels()

    private val pinyinList by lazy {
        arrayOf(
            "ā-a-1声",
            "á-a-2声",
            "ǎ-a-3声",
            "à-a-4声",
            "ê-e-?声",
            "ē-e-1声",
            "é-e-2声",
            "ě-e-3声",
            "è-e-4声",
            "ī-i-1声",
            "í-i-2声",
            "ǐ-i-3声",
            "ì-i-4声",
            "ō-o-1声",
            "ó-o-2声",
            "ǒ-o-3声",
            "ò-o-4声",
            "ū-u-1声",
            "ú-u-2声",
            "ǔ-u-3声",
            "ù-u-4声",
            "ǖ-v-1声",
            "ǘ-v-2声",
            "ǚ-v-3声",
            "ǜ-v-4声"
        )
    }
    private lateinit var data: ReplaceRule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        binding.tilPattern.setEndIconOnClickListener {
            displayPinyinDialog()
        }
        binding.tilReplacement.setEndIconOnClickListener {
            displayPinyinDialog()
        }

        binding.etPattern.addTextChangedListener {
            if (!binding.switchIsRegex.isChecked) {
                binding.etTestText.text?.ifEmpty {
                    binding.etTestText.text = binding.etPattern.text
                }
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initKeyBoardToolTip()
    }

    private fun insertCharToCurrentEditText(char: String) {
        val currentFocusView = currentFocus
        if (currentFocusView is EditText)
            currentFocusView.editableText.insert(
                currentFocusView.selectionStart, char
            )
    }

    private fun initKeyBoardToolTip() {
        val chars =
            listOf("(", ")", "[", "]", "|", "\\", "/", "{", "}", "^", "$", ".", "*", "+", "?")
        for (char in chars) {
            binding.chipGroup.addView(
                Chip(
                    this, null,
                    com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated
                ).apply {
                    text = char
                    setTypeface(null, Typeface.BOLD)
                    setOnClickListener { insertCharToCurrentEditText(char) }
                })
        }

        val keyBoardHelper = KeyBoardHelper(binding.root)
        keyBoardHelper.attachToWindow(window)
        keyBoardHelper.callback = object : KeyBoardHelper.Callback {
            override fun onShow() {
                binding.chipGroup.isVisible = true
            }

            override fun onDismiss() {
                binding.chipGroup.isGone = true
            }
        }

    }

    private fun displayPinyinDialog() {
        val scrollView = NestedScrollView(this)
        val chipGroup = ChipGroup(this).apply {
            setPadding(16.dp)
        }
        scrollView.addView(chipGroup)
        val dlg = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.systts_replace_insert_pinyin)
            .setView(scrollView)
            .setNegativeButton(R.string.cancel, null)
            .show()
        pinyinList.forEach { py ->
            val ss = py.split("-")
            val chipItem = Chip(this)
            chipItem.text = ss[0]
            chipItem.contentDescription = "${chipItem.text}:  $ss[1]"
            chipItem.setTypeface(null, Typeface.BOLD)
            chipItem.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge)
            chipGroup.addView(chipItem)
            chipItem.setOnClickListener {
                dlg.dismiss()
                insertCharToCurrentEditText(py[0].toString())
            }
        }
    }

    private fun doTest() {
        val text = binding.etTestText.text.toString()
        val pattern = binding.etPattern.text.toString()
        val replacement = binding.etReplacement.text.toString()
        val isRegex = binding.switchIsRegex.isChecked

        val result =
            vm.test(text, pattern, replacement, isRegex).ifBlank { getString(R.string.none) }
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