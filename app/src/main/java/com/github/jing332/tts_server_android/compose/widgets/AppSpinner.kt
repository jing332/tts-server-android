package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_server_android.conf.AppConfig
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextFieldSelectionDialog(
    modifier: Modifier,

    label: @Composable () -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,

    value: Any,
    values: List<Any>,
    entries: List<String>,

    onSelectedChange: (key: Any, value: String) -> Unit,
    onValueSame: (current: Any, new: Any) -> Boolean = { current, new -> current == new },
) {
    val selectedText = entries.getOrNull(max(0, values.indexOf(value))) ?: ""
    var expanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(values, entries) {
        values.getOrNull(entries.indexOf(selectedText))?.let {
            onSelectedChange.invoke(it, selectedText)
        }
    }
    if (expanded) {
        /*AppDialog(onDismissRequest = { expanded = false },
            title = label,
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var searchText by rememberSaveable { mutableStateOf("") }

                    DenseOutlinedField(
                        value = searchText, onValueChange = { searchText = it },
                        label = { Text(stringResource(id = R.string.search)) },
                    )

                    val listState = rememberLazyListState(
                        initialFirstVisibleItemIndex = max(0, values.indexOf(value))
                    )

                    val isEmpty by remember {
                        derivedStateOf { listState.layoutInfo.viewportSize == IntSize.Zero }
                    }

                    if (searchText.isNotBlank() && isEmpty)
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .minimumInteractiveComponentSize()
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(id = R.string.empty_list),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                    LazyColumn(
                        Modifier
                            .padding(top = 4.dp)
                            .simpleVerticalScrollbar(listState),
                        state = listState
                    ) {
                        itemsIndexed(entries) { index, str ->
                            if (searchText.isNotBlank() && !str.contains(searchText)) return@itemsIndexed

                            val current = values[index]
                            val selected = onKeySame.invoke(value, current)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Unspecified
                                    )
                                    .clickableRipple {
                                        onSelectedChange.invoke(current, str)
                                        expanded = false
                                    },
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .minimumInteractiveComponentSize()
                                        .align(Alignment.CenterVertically),
                                    text = str,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Start,
                                )
                            }
                        }
                    }
                }
            },
            buttons = {
                TextButton(onClick = { expanded = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )*/
        AppSelectionDialog(
            onDismissRequest = { expanded = false },
            title = label,
            value = value,
            values = values,
            entries = entries,
            onClick = { value, entry ->
                onSelectedChange.invoke(value, entry)
                expanded = false
            },
            onValueSame = onValueSame,
        )
    }

    Box(
        modifier = modifier
            .clickable(
                role = Role.DropdownList,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { expanded = !expanded }
    ) {
        CompositionLocalProvider(
            LocalTextInputService provides null,
            LocalTextToolbar provides EmptyTextToolbar,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface,

                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    disabledBorderColor = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,

                    disabledPrefixColor = MaterialTheme.colorScheme.onSurface,
                    disabledSuffixColor = MaterialTheme.colorScheme.onSurface,
                ),

                leadingIcon = leadingIcon,
                readOnly = true,
                value = selectedText,
                onValueChange = { },
                label = label,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
            )
        }
    }
}

@Composable
fun AppSpinner(
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit),
    leadingIcon: @Composable (() -> Unit)? = null,

    value: Any,
    values: List<Any>,
    entries: List<String>,
    maxDropDownCount: Int = AppConfig.spinnerMaxDropDownCount.value,

    onValueSame: (current: Any, new: Any) -> Boolean = { current, new -> current == new },
    onSelectedChange: (key: Any, value: String) -> Unit,
) {
    if (values.isNotEmpty() && !values.contains(value)) {
        onSelectedChange.invoke(values[0], entries[0])
    }

    if (maxDropDownCount > 0 && values.size > maxDropDownCount) {
        TextFieldSelectionDialog(
            modifier = modifier,
            label = label,
            leadingIcon = leadingIcon,
            value = value,
            values = values,
            entries = entries,
            onValueSame = onValueSame,
            onSelectedChange = onSelectedChange,
        )
    } else
        DropdownTextField(
            modifier = modifier,
            label = label,
            leadingIcon = leadingIcon,
            value = value,
            values = values,
            entries = entries,
            onSelectedChange = onSelectedChange,
            onValueSame = onValueSame,
        )

//    LaunchedEffect(keys) {
//        keys.getOrNull(values.indexOf(selectedText))?.let {
//            onSelectedChange.invoke(it, selectedText)
//        }
//    }


}


@Preview
@Composable
private fun ExposedDropTextFieldPreview() {
    var key by remember { mutableIntStateOf(1) }
    val list = 0.rangeTo(10).toList()
    AppSpinner(
        label = { Text("所属分组") },
        value = key,
        values = list,
        entries = list.map { it.toString() },
        maxDropDownCount = 2
    ) { k, _ ->
        key = k as Int
    }
}