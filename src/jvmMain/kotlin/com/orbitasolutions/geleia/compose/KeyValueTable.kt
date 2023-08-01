package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.orbitasolutions.geleia.extensions.insertAtIndex

@Composable
fun KeyValueTable(
    map: LinkedHashMap<String, String?>,
    readOnly: Boolean = false,
    allowDisable: Boolean = false,
    change: (LinkedHashMap<String, String?>) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.animateScrollTo(100) }

    if (map.keys.lastOrNull()?.isNotBlank() != false)
        map[""] = ""

    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp).verticalScroll(scrollState)) {
        if (allowDisable) {
            Column {
                Row(Modifier.padding(vertical = 5.dp)) {
                    Text("", style = MaterialTheme.typography.caption, modifier = Modifier.alpha(ContentAlpha.disabled))
                }
                map.onEach { entry ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Checkbox(
                            true, onCheckedChange = {
//                            map[entry.key] = it
//                            change(map)
                            },
                            modifier = Modifier.height(38.dp)
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(1f).padding(start = if (allowDisable) 0.dp else 10.dp)) {
            Row(Modifier.padding(vertical = 5.dp)) {
                Text("Key", style = MaterialTheme.typography.caption, modifier = Modifier.alpha(ContentAlpha.disabled))
            }
            map.onEachIndexed { index, entry ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    if (readOnly) {
                        Text(text = entry.key, style = MaterialTheme.typography.subtitle2)
                    } else {
                        CustomTextField(
                            value = entry.key,
                            onValueChange = {
                                map.insertAtIndex(it, entry.value, index)
                                map.remove(entry.key)
                                change(map)
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(4f).padding(horizontal = 10.dp)) {
            Row(Modifier.padding(vertical = 5.dp)) {
                Text(
                    "Value",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alpha(ContentAlpha.disabled)
                )
            }
            map.onEach { entry ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    if (readOnly) {
                        Text(text = entry.value ?: "", style = MaterialTheme.typography.subtitle2)
                    } else {
                        CustomTextField(
                            value = entry.value ?: "",
                            onValueChange = {
                                map[entry.key] = it
                                change(map)
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp).padding(0.dp)
                        )
                    }
                }
            }
        }
    }
}