package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.orbitasolutions.geleia.utils.KeyStringValue
import java.util.LinkedList

@Composable
fun KeyValueTable(
    items: LinkedList<KeyStringValue>,
    readOnly: Boolean = false,
    allowDisable: Boolean = false,
    onChange: (LinkedList<KeyStringValue>) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.animateScrollTo(100) }

    if (items.lastOrNull()?.key?.isNotBlank() != false)
        items.add(KeyStringValue(""))

    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp).verticalScroll(scrollState)) {
        if (allowDisable) {
            Column {
                Row(Modifier.padding(vertical = 5.dp)) {
                    Text("", style = MaterialTheme.typography.caption, modifier = Modifier.alpha(ContentAlpha.disabled))
                }
                items.onEachIndexed { index, entry ->
                    Row(Modifier.padding(vertical = 5.dp).height(38.dp).width(50.dp)) {
                        Checkbox(
                            enabled = index < items.size - 1,
                            checked = !entry.disabled && index < items.size - 1,
                            onCheckedChange = {
                                items[index] = entry.copy(disabled = !it)
                                onChange(items)
                            },
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(1f).padding(start = if (allowDisable) 0.dp else 10.dp)) {
            Row(Modifier.padding(vertical = 5.dp)) {
                Text("Key", style = MaterialTheme.typography.caption, modifier = Modifier.alpha(ContentAlpha.disabled))
            }
            items.onEachIndexed { index, entry ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    if (readOnly) {
                        Text(text = entry.key, style = MaterialTheme.typography.subtitle2)
                    } else {
                        CustomTextField(
                            id = entry.index.toString(),
                            value = entry.key,
                            onChange = {
                                items[index] = entry.copy(key = it)
                                if (entry.key != it) {
                                    onChange(items)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(4f).padding(start = 10.dp)) {
            Row(Modifier.padding(vertical = 5.dp)) {
                Text(
                    "Value",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alpha(ContentAlpha.disabled)
                )
            }
            items.onEachIndexed { index, entry ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    if (readOnly) {
                        Text(text = entry.value, style = MaterialTheme.typography.subtitle2)
                    } else {
                        CustomTextField(
                            id = entry.index.toString(),
                            value = entry.value,
                            onChange = {
                                items[index] = entry.copy(value = it)
                                if (entry.value != it) {
                                    onChange(items)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp).padding(0.dp)
                        )
                    }
                }
            }
        }
        Column {
            Row(Modifier.padding(vertical = 5.dp)) {
                Text("", style = MaterialTheme.typography.caption, modifier = Modifier.alpha(ContentAlpha.disabled))
            }
            items.onEachIndexed { index, _ ->
                Row(Modifier.padding(vertical = 5.dp).height(38.dp).width(50.dp)) {
                    IconButton(
                        modifier = Modifier.fillMaxHeight()
                            .alpha(if (index < items.size - 1) ContentAlpha.medium else ContentAlpha.disabled),
                        enabled = index < items.size - 1,
                        onClick = {
                            items.removeAt(index)
                            onChange(items)
                        }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}