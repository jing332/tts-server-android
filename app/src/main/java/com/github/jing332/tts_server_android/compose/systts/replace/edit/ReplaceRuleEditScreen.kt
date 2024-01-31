@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import com.github.jing332.tts_server_android.compose.systts.AuditionDialog
import com.github.jing332.tts_server_android.compose.widgets.AppSpinner
import com.github.jing332.tts_server_android.compose.widgets.TextCheckBox
import com.github.jing332.tts_server_android.conf.ReplaceRuleConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
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
    val inputKeyState = remember { mutableStateOf("") }
//    var toolbarKeyList: List<Pair<String, String>> by rememberDataSaverState(
//        key = ConfigConst.KEY_SOFT_KEYBOARD_TOOLBAR,
//        default = emptyList()
//    )
    var toolBarSymbols by remember { ReplaceRuleConfig.symbols }
    var showToolbarSettingsDialog by remember { mutableStateOf(false) }
    if (showToolbarSettingsDialog) {
        ToolBarSettingsDialog(
            onDismissRequest = { showToolbarSettingsDialog = false },
            symbols = toolBarSymbols,
            onSave = {
                toolBarSymbols = it
                showToolbarSettingsDialog = false
            },
            onReset = {
                toolBarSymbols = ReplaceRuleConfig.defaultSymbols
                showToolbarSettingsDialog = false
            }
        )
    }

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

//                IconButton(onClick = {}) {
//                    Icon(
//                        Icons.Filled.MoreVert, stringResource(id = R.string.more_options)
//                    )
//                }
            })
    }, bottomBar = {
        SoftKeyboardInputToolbar(symbols = toolBarSymbols, onClick = {
            inputKeyState.value = it
        }, onSettings = {
            showToolbarSettingsDialog = true
        })
    }, content = { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
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

                name = rule.name,
                onNameChange = {
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

                sampleText = rule.sampleText,
                onSampleTextChange = {
                    onRuleChange.invoke(rule.copy(sampleText = it))
                },

                onTest = {
                    (try {
                        vm.doReplace(rule, it)
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
    const val NAME = "name"
    const val PATTERN = "pattern"
    const val REPLACEMENT = "replacement"
    const val SAMPLE_TEXT = "sample_text"
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

    name: String,
    onNameChange: (String) -> Unit,
    patternValue: String,
    onReplaceValueChange: (String) -> Unit,
    replacementValue: String,
    onReplacementValueChange: (String) -> Unit,
    isRegex: Boolean,
    onIsRegexChange: (Boolean) -> Unit,

    sampleText: String,
    onSampleTextChange: (String) -> Unit,

    onTest: (String) -> String,
) {
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
        AppSpinner(
            label = { Text(text = stringResource(R.string.belonging_group)) },
            value = group,
            values = groupKeys,
            entries = groupValues,
            onSelectedChange = { value, _ ->
                onGroupChange.invoke(value as ReplaceRuleGroup)
            }
        )

        TextFieldInsert(
            label = { Text(stringResource(R.string.name)) },
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth(),
            inertKeyState = insertKeyState,
        )

        TextFieldInsert(
            label = { Text(stringResource(R.string.replace_rule)) },
            value = patternValue,
            onValueChange = onReplaceValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            inertKeyState = insertKeyState,
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.systts_replace_insert_pinyin))
                }
            }
        )

        TextFieldInsert(
            label = { Text(stringResource(R.string.replacement)) },
            value = replacementValue,
            onValueChange = onReplacementValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            inertKeyState = insertKeyState,
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.systts_replace_insert_pinyin))
                }
            }
        )

        TextCheckBox(text = {
            Text(text = stringResource(R.string.systts_replace_use_regex))
        }, checked = isRegex, onCheckedChange = onIsRegexChange)

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        var testResult by remember { mutableStateOf("") }
        TextFieldInsert(
            label = { Text(stringResource(R.string.test)) },
            value = sampleText,
            onValueChange = {
                onSampleTextChange(it)
                testResult = onTest(it)
            },
            modifier = Modifier
                .fillMaxWidth(),
            inertKeyState = insertKeyState,
            trailingIcon = {
                var showAuditionDialog by remember { mutableStateOf<SystemTts?>(null) }
                if (showAuditionDialog != null) {
                    AuditionDialog(
                        onDismissRequest = { showAuditionDialog = null },
                        systts = showAuditionDialog!!,
                        text = testResult,
                    )
                }

                var showTtsSelectDialog by remember { mutableStateOf(false) }
                if (showTtsSelectDialog) {
                    SysttsSelectBottomSheet(onDismissRequest = { showTtsSelectDialog = false }) {
                        showTtsSelectDialog = false
                        showAuditionDialog = it
                    }
                }

                AnimatedVisibility(visible = testResult.isNotBlank()) {
                    IconButton(onClick = {
                        showTtsSelectDialog = true
                    }) {
                        Icon(Icons.Filled.Headset, stringResource(R.string.click_play))
                    }
                }
            }
        )

        if (sampleText.isNotEmpty()) Text(stringResource(R.string.label_result))
        SelectionContainer {
            Text(text = testResult, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun TextFieldInsert(
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,

    inertKeyState: MutableState<String>,
) {
    var fieldValue by remember() { mutableStateOf(TextFieldValue()) }
    LaunchedEffect(key1 = value) {
        fieldValue = fieldValue.copy(text = value)
    }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = inertKeyState.value) {
        if (!isFocused || inertKeyState.value.isEmpty()) return@LaunchedEffect

        fieldValue = fieldValue.newValueOfInsertText(inertKeyState.value)
        onValueChange(fieldValue.text)

        inertKeyState.value = ""
    }
    OutlinedTextField(
        label = label,
        value = fieldValue,
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
            },
        onValueChange = {
            fieldValue = it
            onValueChange.invoke(it.text)
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
    )
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