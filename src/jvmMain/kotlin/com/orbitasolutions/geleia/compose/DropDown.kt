package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DropDown(
    set: Set<String>,
    selected: String = set.first(),
    modifier: Modifier = Modifier,
    select: (String) -> Unit
) {
    var requestMethodExpanded by remember { mutableStateOf(false) }
    OutlinedButton(
        contentPadding = PaddingValues(0.dp),
        modifier = modifier,
        onClick = { requestMethodExpanded = true }) {
        Text(
            selected,
            modifier = Modifier.padding(vertical = 12.dp).sizeIn(minWidth = 200.dp),
            color = MaterialTheme.colors.onPrimary,
            style = TextStyle(textAlign = TextAlign.Center)
        )
        DropdownMenu(
            expanded = requestMethodExpanded,
            onDismissRequest = { requestMethodExpanded = false }
        ) {
            set.forEach {
                DropdownMenuItem(onClick = {
                    select(it)
                    requestMethodExpanded = false
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(it)
                }
            }
        }
    }
}