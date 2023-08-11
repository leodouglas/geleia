package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.orbitasolutions.geleia.extensions.MaterialAppearance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImportDialog(window: WindowPosition, onClosed: () -> Unit, addCurl: (String) -> Unit) {
    var curl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val curlFocusRequester = FocusRequester()
    val rememberDialogState = rememberDialogState(position = window)

    Dialog(onCloseRequest = { onClosed() }, state = rememberDialogState) {
        MaterialAppearance {
            Column {
                OutlinedTextField(
                    value = curl,
                    onValueChange = { curl = it },
                    label = { Text("curl") },
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .weight(1f).focusRequester(curlFocusRequester)
                )
                Row(Modifier.align(Alignment.End)) {
                    Button(onClick = {
                        addCurl(curl)
                        curl = ""
                        onClosed()
                    }, Modifier.padding(start = 10.dp)) {
                        Text("Import")
                    }
                }
            }
        }
    }
    scope.launch {
        delay(100)
        curlFocusRequester.requestFocus()
    }
}