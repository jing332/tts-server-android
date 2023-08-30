package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.max

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropdownTextField(
    modifier: Modifier = Modifier,
    label: @Composable() (() -> Unit),
    key: Any,
    keys: List<Any>,
    values: List<String>,
    leadingIcon: @Composable (() -> Unit)? = null,
    onKeySame: (current: Any, new: Any) -> Boolean = { current, new -> current == new },
    onSelectedChange: (key: Any, value: String) -> Unit,
) {
    var selectedText = values.getOrNull(max(0, keys.indexOf(key))) ?: ""
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(keys) {
        keys.getOrNull(values.indexOf(selectedText))?.let {
            onSelectedChange.invoke(it, selectedText)
        }
    }

    CompositionLocalProvider(
        LocalTextInputService provides null // Disable Keyboard
    ) {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                leadingIcon = leadingIcon,
                readOnly = true,
                value = selectedText,
                onValueChange = { },
                label = label,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                values.forEachIndexed { index, text ->
                    val checked = onKeySame(key, keys[index])
                    DropdownMenuItem(
                        text = {
                            Text(
                                text,
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            expanded = false
                            selectedText = text
                            onSelectedChange.invoke(keys[index], text)
                        }/*, modifier = Modifier.background(
                        if (checked) MaterialTheme.colorScheme.surfaceVariant
                        else Color.TRANSPARENT*/
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun PreviewDropdownTextField() {
    var key by remember { mutableIntStateOf(1) }
    AppSpinner(
        label = { Text("所属分组") },
        value = key,
        values = listOf(1, 2, 3),
        entries = listOf("1", "2", "3"),
    ) { k, _ ->
        key = k as Int
    }
}