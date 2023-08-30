package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.utils.clickableRipple
import com.github.jing332.tts_server_android.utils.simpleVerticalScrollbar
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
    onKeySame: (current: Any, new: Any) -> Boolean = { current, new -> current == new },
) {
    val selectedText = entries.getOrNull(max(0, values.indexOf(value))) ?: ""
    var expanded by rememberSaveable { mutableStateOf(false) }

    if (expanded) {
        AppDialog(onDismissRequest = { expanded = false },
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
                                        if (selected) LocalRippleTheme.current
                                            .defaultColor()
                                            .copy(alpha = LocalRippleTheme.current.rippleAlpha().focusedAlpha)
                                        else Color.Transparent
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
        )
    }

    Box(
        modifier = modifier
            .clickable(role = Role.DropdownList) { expanded = !expanded }
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

    onKeySame: (current: Any, new: Any) -> Boolean = { current, new -> current == new },
    onSelectedChange: (key: Any, value: String) -> Unit,
) {
    if (maxDropDownCount > 0 && values.size > maxDropDownCount) {
        TextFieldSelectionDialog(
            modifier = modifier,
            label = label,
            leadingIcon = leadingIcon,
            value = value,
            values = values,
            entries = entries,
            onKeySame = onKeySame,
            onSelectedChange = onSelectedChange,
        )
    } else
        DropdownTextField(
            modifier = modifier,
            label = label,
            leadingIcon = leadingIcon,
            key = value,
            keys = values,
            values = entries,
            onSelectedChange = onSelectedChange,
            onKeySame = onKeySame,
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
    AppSpinner(
        label = { Text("所属分组") },
        value = key,
        values = listOf(1, 2, 3),
        entries = listOf("1", "2", "3"),
        maxDropDownCount = 2
    ) { k, _ ->
        key = k as Int
    }
}