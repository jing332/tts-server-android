@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.widgets.ExposedDropTextField
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import androidx.compose.material3.AlertDialog as AlertDialog1


@Preview
@Composable
fun PreviewRuleEditScreen() {
    var rule by remember {
        mutableStateOf(
            ReplaceRule(
                name = "test",
                pattern = "test",
                replacement = "test",
                isRegex = false,
            )
        )
    }
    RuleEditScreen(
        rule = rule,
        onRuleChange = { rule = it },
        groups = listOf(
            ReplaceRuleGroup(0, "name1"),
            ReplaceRuleGroup(0, "name2")
        ),
        onSave = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    rule: ReplaceRule,
    onRuleChange: (ReplaceRule) -> Unit,
    groups: List<ReplaceRuleGroup> = remember { appDb.replaceRuleDao.allGroup },
    onSave: () -> Unit,
) {
    val group = remember(rule.groupId) { groups.find { it.id == rule.groupId } ?: groups.first() }

    val vm: RuleEditViewModel = viewModel()
    var inputKeyState = remember { mutableStateOf("") }
//    var toolbarKeyList: List<Pair<String, String>> by rememberDataSaverState(
//        key = ConfigConst.KEY_SOFT_KEYBOARD_TOOLBAR,
//        default = emptyList()
//    )

//    var isVisibleEditToolbarDialog by remember { mutableStateOf(false) }
//    if (isVisibleEditToolbarDialog)
//        EditToolbarKeyDialog(keys = toolbarKeyList, onResult = {
//            isVisibleEditToolbarDialog = false
//            it?.let { list ->
//                println(list.getOrNull(0))
//                toolbarKeyList = list
//            }
//        })
    val navController = LocalNavController.current
    Scaffold(modifier = Modifier, topBar = {
        TopAppBar(modifier = Modifier.fillMaxWidth(),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.nav_back)
                    )
                }
            },
            title = { Text(text = stringResource(id = R.string.replace_rule)) },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//            ),
            actions = {
                IconButton(onClick = {
                    navController.popBackStack()
                    onSave()
                }) {
                    Icon(Icons.Filled.Save, stringResource(id = R.string.save))
                }

                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.MoreVert, stringResource(id = R.string.more_options)
                    )
                }
            })
    }, bottomBar = {
//        if (LocalSoftKeyboardVisible.current.value) {
//            if (toolbarKeyList.none { it.first.isNotBlank() && it.second.isNotBlank() }) {
//                // @formatter:off
//                toolbarKeyList = listOf("(", ")", "[", "]", "{", "}", "<", ">", "!", "@", "#", "$", "%", "^", "&", "*", "-", "+", "=", "|", "\\", "/", "?", ",", ":", ";", "\"", "'").map { it to it }
//                // @formatter:on
//            }
//
//            SoftKeyboardInputToolbar(items = toolbarKeyList, onClick = {
//                inputKeyState.value = it
//            }, onSettings = {
//                isVisibleEditToolbarDialog = true
//            })
//        }
    }, content = { pad ->
        Surface(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
        ) {
            Screen(
                inputKeyState,
                group = group,
                groupKeys = groups,
                groupValues = groups.map { it.name },
                onGroupChange = {
                    onRuleChange.invoke(rule.copy(groupId = it.id))
                },

                nameValue = rule.name,
                onNameValueChange = {
                    onRuleChange.invoke(rule.copy(name = it))
                },

                patternValue = rule.pattern,
                onReplaceValueChange = {
                    onRuleChange.invoke(rule.copy(pattern = it))
                },

                replacementValue = rule.replacement,
                onReplacementValueChange = {
                    onRuleChange.invoke(rule.copy(replacement = it))
                },

                isRegex = rule.isRegex,
                onIsRegexChange = {
                    onRuleChange.invoke(rule.copy(isRegex = it))
                },


                onTest = {
                    (try {
                        vm.doReplace(it)
                    } catch (e: Exception) {
                        e.message ?: ""
                    })
                },
            )
        }
    }
    )
}

private object InputFieldID {
    const val NAME = 0
    const val PATTERN = 1
    const val REPLACEMENT = 2
    const val SAMPLE_TEXT = 3
}

/**
 * 插入文本到当前光标前方
 */
fun TextFieldValue.newValueOfInsertText(
    text: String, cursorPosition: Int = selection.end
): TextFieldValue {
    val newText = StringBuilder(this.text).insert(cursorPosition, text).toString()
    return TextFieldValue(newText, TextRange(cursorPosition + text.length))
}

@Composable
private fun Screen(
    insertKeyState: MutableState<String>,
    group: ReplaceRuleGroup,
    groupKeys: List<ReplaceRuleGroup>,
    groupValues: List<String>,
    onGroupChange: (ReplaceRuleGroup) -> Unit,

    nameValue: String,
    onNameValueChange: (String) -> Unit,
    patternValue: String,
    onReplaceValueChange: (String) -> Unit,
    replacementValue: String,
    onReplacementValueChange: (String) -> Unit,
    isRegex: Boolean,
    onIsRegexChange: (Boolean) -> Unit,

    onTest: (String) -> String,
) {
    var currentInputFocus by remember { mutableIntStateOf(-1) }

    var nameTextFieldValue by remember { mutableStateOf(TextFieldValue(nameValue)) }
    fun setName(value: TextFieldValue) {
        nameTextFieldValue = value
        onNameValueChange.invoke(value.text)
    }

    var patternTextFieldValue by remember { mutableStateOf(TextFieldValue(patternValue)) }
    fun setPattern(value: TextFieldValue) {
        patternTextFieldValue = value
        onReplaceValueChange.invoke(value.text)
    }

    var replacementTextFieldValue by remember { mutableStateOf(TextFieldValue(replacementValue)) }
    fun setReplacement(value: TextFieldValue) {
        replacementTextFieldValue = value
        onReplacementValueChange.invoke(value.text)
    }

    var sampleTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    if (insertKeyState.value.isNotEmpty()) {
        when (currentInputFocus) {
            InputFieldID.NAME ->
                setName(nameTextFieldValue.newValueOfInsertText(insertKeyState.value))

            InputFieldID.PATTERN -> setPattern(
                patternTextFieldValue.newValueOfInsertText(insertKeyState.value)
            )

            InputFieldID.REPLACEMENT ->
                setReplacement(replacementTextFieldValue.newValueOfInsertText(insertKeyState.value))

            InputFieldID.SAMPLE_TEXT ->
                sampleTextFieldValue =
                    sampleTextFieldValue.newValueOfInsertText(insertKeyState.value)
        }
        insertKeyState.value = ""
    }


    var isVisiblePinyinDialog by remember { mutableStateOf(false) }
    if (isVisiblePinyinDialog) PinyinDialog({ isVisiblePinyinDialog = false }, onInput = {
        isVisiblePinyinDialog = false
        insertKeyState.value = it
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropTextField(label = { Text(text = stringResource(R.string.belonging_group)) },
            key = group,
            keys = groupKeys,
            values = groupValues,
            onSelectedChange = { value, _ ->
                onGroupChange.invoke(value as ReplaceRuleGroup)
            }
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.name)) },
            value = nameTextFieldValue,
            onValueChange = { setName(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) currentInputFocus = InputFieldID.NAME
                },
        )

        OutlinedTextField(label = { Text(stringResource(R.string.replace_rule)) },
            value = patternTextFieldValue,
            onValueChange = { setPattern(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) currentInputFocus = InputFieldID.PATTERN
                },
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.systts_replace_insert_pinyin))
                }
            })
        OutlinedTextField(label = { Text(stringResource(R.string.systts_replace_as)) },
            value = replacementTextFieldValue,
            onValueChange = {
                replacementTextFieldValue = it
                onReplacementValueChange.invoke(it.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) currentInputFocus = InputFieldID.REPLACEMENT
                },
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.systts_replace_insert_pinyin))
                }
            })

        Row(modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable {
                onIsRegexChange(!isRegex)
            }) {
            Checkbox(
                checked = isRegex,
                onCheckedChange = onIsRegexChange,
            )
            Text(
                text = stringResource(R.string.systts_replace_use_regex),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        var testResult by remember { mutableStateOf("") }

        OutlinedTextField(
            label = { Text(stringResource(R.string.test)) },
            value = sampleTextFieldValue,
            onValueChange = {
                sampleTextFieldValue = it
                testResult = onTest.invoke(it.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) currentInputFocus = InputFieldID.SAMPLE_TEXT
                },
        )

        if (sampleTextFieldValue.text.isNotEmpty()) Text(stringResource(R.string.label_result))
        SelectionContainer {
            Text(text = testResult, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PinyinDialog(onDismissRequest: () -> Unit, onInput: (text: String) -> Unit) {
    val pinyinList = remember {
        listOf(
            listOf(
                "ā-a-1声",
                "á-a-2声",
                "ǎ-a-3声",
                "à-a-4声",
            ), listOf(
                "ê-e-?声",
                "ē-e-1声",
                "é-e-2声",
                "ě-e-3声",
                "è-e-4声",
            ), listOf(
                "ī-i-1声",
                "í-i-2声",
                "ǐ-i-3声",
                "ì-i-4声",
            ), listOf(
                "ō-o-1声",
                "ó-o-2声",
                "ǒ-o-3声",
                "ò-o-4声",
            ), listOf(
                "ū-u-1声",
                "ú-u-2声",
                "ǔ-u-3声",
                "ù-u-4声",
            ), listOf(
                "ǖ-v-1声", "ǘ-v-2声", "ǚ-v-3声", "ǜ-v-4声"
            )
        )
    }

    AlertDialog1(onDismissRequest = onDismissRequest) {
        Surface(
            color = AlertDialogDefaults.containerColor,
            shape = AlertDialogDefaults.shape,
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.systts_replace_insert_pinyin),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall,
                )

                LazyColumn {
                    items(pinyinList) { item ->
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            item.forEach {
                                TextButton(
                                    modifier = Modifier.semantics {
                                        text = AnnotatedString(it)
                                    },
                                    onClick = { onInput.invoke(it[0].toString()) }) {
                                    Text(
                                        text = it[0].toString(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                        .padding(end = 16.dp),
                    onClick = { onDismissRequest.invoke() }) {
                    Text(text = stringResource(id = R.string.cancel))
                }

            }
        }
    }

}

@Preview
@Composable
fun PreviewPinyinDialog() {
    var isVisible by remember { mutableStateOf(true) }
    if (isVisible) PinyinDialog({ isVisible = false }, onInput = {})
}