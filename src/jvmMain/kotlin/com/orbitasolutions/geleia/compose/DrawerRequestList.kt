package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orbitasolutions.geleia.domains.Request
import com.orbitasolutions.geleia.domains.RequestVar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerState.DrawerRequestList(
    scope: CoroutineScope,
    requests: List<Request>,
    vars: Set<RequestVar>,
    varGroupSelected: String,
    selectIndex: (Int) -> Unit,
    selectVarGroup: (String) -> Unit,
    newRequest: () -> Unit,
    importRequest: () -> Unit,
    deleteRequest: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.animateScrollTo(100) }

    Row {
        Surface(elevation = DrawerDefaults.Elevation) {
            Column(Modifier.fillMaxHeight().width(500.dp)) {
                TopAppBar(
                    modifier = Modifier.height(50.dp),
                    title = { Text(text = "") },
                    actions = {
                        IconButton(onClick = {
                            scope.launch { close() }
                        }) {
                            Icon(
                                modifier = Modifier.padding(all = 10.dp),
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                )
                Column(Modifier.weight(1f).verticalScroll(scrollState)) {
                    requests.mapIndexed { index, request ->
                        TextButton(
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            onClick = {
                                scope.launch {
                                    selectIndex(index)
                                    close()
                                }
                            }) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                Row(modifier = Modifier.width(30.dp).padding(start = 14.dp, end = 8.dp)) {
                                    if (request.modified) {
                                        Icon(
                                            modifier = Modifier.fillMaxSize()
                                                .align(Alignment.CenterVertically),
                                            imageVector = Icons.Default.Circle,
                                            tint = Color(0xFFF96C38),
                                            contentDescription = "Unsaved"
                                        )
                                    }
                                }
                                Text(
                                    text = request.method.short,
                                    modifier = Modifier.padding(end = 6.dp),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.body2,
                                    color = coloringMethods(request.method)
                                )
                                Text(
                                    text = request.name,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            deleteRequest(index)
                                        }
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
                    Divider()
                    Row {
                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = MaterialTheme.colors.onPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    newRequest()
                                    close()
                                }
                            }) {
                            Icon(
                                modifier = Modifier.padding(all = 10.dp),
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Request"
                            )
                            Text("New request")
                        }
                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = MaterialTheme.colors.onPrimary
                            ), modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    importRequest()
                                    close()
                                }
                            }) {
                            Icon(
                                modifier = Modifier.padding(all = 10.dp),
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Import Request"
                            )
                            Text("Import request")
                        }
                    }
                }
                if (vars.map(RequestVar::group).distinct().any { it != "DEFAULT" }) {
                    BottomAppBar {
                        Text("Variables:", Modifier.padding(horizontal = 10.dp))
                        DropDown(hashSetOf("DEFAULT").also {
                            it += vars.map(RequestVar::group)
                        }, varGroupSelected, modifier = Modifier.height(38.dp), select = selectVarGroup)
                    }
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize().clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            scope.launch { close() }
        }) { }
    }
}