package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
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
import com.orbitasolutions.geleia.theme.jetbrainsDarkColors
import com.orbitasolutions.geleia.theme.jetbrainsTypo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImportDialog(window: WindowPosition, onClosed: () -> Unit, addCurl: (String) -> Unit) {
    var curl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val curlFocusRequester = FocusRequester()
    val rememberDialogState = rememberDialogState(
        position = window
    )

    Dialog(onCloseRequest = { onClosed() }, state = rememberDialogState) {

        with(this.window.rootPane) {
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
            putClientProperty("apple.awt.windowTitleVisible", false)

            putClientProperty("jetbrains.awt.windowDarkAppearance", true)
            putClientProperty("jetbrains.awt.transparentTitleBarAppearance", true)
        }

        MaterialTheme(colors = jetbrainsDarkColors(), typography = jetbrainsTypo()) {
            Surface(shape = RoundedCornerShape(8.dp), elevation = DrawerDefaults.Elevation) {
                WindowDraggableArea {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxWidth().padding(4.dp).padding(top = 20.dp)) {
                            OutlinedTextField(
                                value = curl,
                                onValueChange = { curl = it },
                                label = { Text("curl") },
                                textStyle = TextStyle(fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp)
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
            }
        }
    }
    scope.launch {
        delay(100)
        curlFocusRequester.requestFocus()
    }
}