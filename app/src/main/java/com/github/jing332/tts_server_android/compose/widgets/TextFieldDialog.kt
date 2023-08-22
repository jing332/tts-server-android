package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TextFieldDialog(
    title: String,
    text: String,
    onTextChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(value = text, onValueChange = onTextChange)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        }
    )
}