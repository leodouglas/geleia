package com.orbitasolutions.geleia.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import com.orbitasolutions.geleia.domains.*
import com.orbitasolutions.geleia.extensions.MaterialAppearance
import com.orbitasolutions.geleia.extensions.onClose
import com.orbitasolutions.geleia.extensions.rememberPrefWindowState
import com.orbitasolutions.geleia.extensions.savePrefWindowState
import com.orbitasolutions.geleia.services.RequestService
import com.orbitasolutions.geleia.utils.getProjectPref
import com.orbitasolutions.geleia.utils.setProjectPref
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeThemeType
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Taskbar
import java.util.*
import kotlin.concurrent.thread

val codeStyleSource = TextStyle(
    fontSize = 13.sp,
    fontFamily = FontFamily.Monospace,
    lineHeight = 17.sp,
    fontWeight = FontWeight.Thin
)

@Preview
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun WindowScope.app() {

    onClose {
        savePrefWindowState("main", window)
    }

    //request remembers
    var requests by remember { mutableStateOf(RequestService.loadFileRequests()) }
    val vars by remember { mutableStateOf(RequestService.loadVars()) }
    var varGroup by remember { mutableStateOf("DEFAULT") }
    var requestIndex by remember { mutableStateOf(getProjectPref("lastRequestIndex", "0").toInt()) }
    if (requestIndex > requests.size) requestIndex = 0
    val request = requests[requestIndex]
    var lastRequestId by remember { mutableStateOf(0) }
    var responseProgress by remember { mutableStateOf("") }
    var response: Response? by remember { mutableStateOf(null) }
    var url by remember {
        mutableStateOf(
            TextFieldValue(
                annotatedString = annotatedStr(
                    request.url,
                    CodeLang.Param
                )
            )
        )
    }
    var originalName by remember { mutableStateOf(request.name) }

    fun onChangeRequest(change: Request, save: Boolean = false) {
        requests[requestIndex] = change.copy(modified = !save)
        requests = RequestList(requests.toList())
    }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    //front remembers
    var requesting by remember { mutableStateOf(false) }
    var responseVisible by remember { mutableStateOf(false) }
    var responseInPopup by remember { mutableStateOf(false) }
    val responseFocusRequest = FocusRequester()
    val nameFocusRequest = FocusRequester()
    val urlFocusRequest = FocusRequester()
    var requestTabItemsSelected by remember { mutableStateOf(defaultTabRequest(request)) }
    var editingName by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    fun loadRequest(index: Int, show: Boolean = true) {
        val nextRequest = requests[index]
        requestIndex = index
        setProjectPref("lastRequestIndex", requestIndex.toString())
        url = TextFieldValue(annotatedString = annotatedStr(nextRequest.url, CodeLang.Param))
        originalName = nextRequest.name
        editingName = false
        requesting = false
        if (show) {
            response = null
            responseVisible = false
            requestTabItemsSelected = defaultTabRequest(nextRequest)
            urlFocusRequest.requestFocus()
        }
    }

    fun newRequest(newRequest: Request? = null) {
        requests = RequestList(requests.also {
            if (newRequest != null) {
                it.add(newRequest.copy(modified = true))
            } else {
                it.add(Request(modified = true))
            }
        }.toList())
        loadRequest(requests.size - 1)
    }

    fun deleteRequest(index: Int) {
        requests.removeAt(index)
        requests = RequestList(requests.toList())
        if (requests.isEmpty()) {
            newRequest()
        } else {
            if (index < requestIndex) {
                loadRequest(requestIndex - 1)
            } else if (index == requestIndex) {
                loadRequest(0)
            }
        }
        RequestService.saveFileRequests(vars, requests)
    }

    fun importRequest() {
        showImportDialog = true
    }

    fun selectVarGroup(group: String) {
        varGroup = group
    }

    if (showImportDialog) {
        ImportDialog(
            window = WindowPosition(
                (this.window.location.x + (this.window.size.width / 2) - 100).dp,
                (this.window.location.y + (this.window.size.height / 2) - 200).dp
            ),
            onClosed = {
                showImportDialog = false
            }) {
            val newRequest = RequestService.convertCurlCommand(it)
            newRequest(newRequest)
        }
    }

    Scaffold(
        topBar = {
            WindowDraggableArea {
                Surface(elevation = AppBarDefaults.TopAppBarElevation) {
                    Row(Modifier.padding(start = 70.dp).height(50.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(modifier = Modifier.fillMaxHeight().padding(10.dp),
                                onClick = {
                                    scope.launch { scaffoldState.drawerState.open() }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }

                            if (!editingName) {
                                Text(text = request.name,
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.onClick(onDoubleClick = {
                                        editingName = true
                                        scope.launch {
                                            delay(100)
                                            nameFocusRequest.requestFocus()
                                        }
                                    }) {})
                            } else {
                                OutlinedTextField(
                                    value = request.name,
                                    singleLine = true,
                                    onValueChange = {
                                        onChangeRequest(request.copy(name = it))
                                    },
                                    modifier = Modifier.focusRequester(nameFocusRequest)
                                )
                                IconButton(modifier = Modifier.padding(start = 2.dp),
                                    onClick = {
                                        editingName = false
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Ok"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editingName = false
                                        onChangeRequest(request.copy(name = originalName))
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Cancel"
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxHeight().weight(1f).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (request.command != null) {
                                IconButton(enabled = request.modified,
                                    modifier = Modifier.size(44.dp),
                                    onClick = {
                                        scope.launch {
                                            onChangeRequest(
                                                RequestService.restore(request),
                                                save = true
                                            )
                                            loadRequest(requestIndex, true)
                                        }
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Restore"
                                    )
                                }
                            }
                            IconButton(enabled = request.modified, modifier = Modifier.size(44.dp),
                                onClick = {
                                    scope.launch {
                                        onChangeRequest(
                                            request.copy(command = RequestService.generateCurl(request)),
                                            save = true
                                        )
                                        RequestService.saveFileRequests(vars, requests)
                                        loadRequest(requestIndex, false)
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        }
                    }
                }
            }
        },

        bottomBar = {
            BottomAppBar {
                Row(
                    Modifier.fillMaxHeight().weight(1f).padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (response?.statusCode?.code) {
                        in 200..299 -> {
                            Icon(
                                modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = response?.statusCode?.name,
                                tint = Color.Green
                            )
                        }

                        in 300..399 -> {
                            Icon(
                                modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                                imageVector = Icons.Default.SyncProblem,
                                contentDescription = response?.statusCode?.name,
                                tint = Color.Yellow
                            )
                        }

                        in 400..499 -> {
                            Icon(
                                modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                                imageVector = Icons.Default.Warning,
                                contentDescription = response?.statusCode?.name,
                                tint = Color.Yellow
                            )
                        }

                        in 500..599 -> {
                            Icon(
                                modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                                imageVector = Icons.Default.Error,
                                contentDescription = response?.statusCode?.name,
                                tint = Color.Red
                            )
                        }

                        null -> {}
                        else -> {
                            Icon(
                                modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                                imageVector = Icons.Default.Warning,
                                contentDescription = response?.statusCode?.name,
                                tint = Color.Yellow
                            )
                        }
                    }
                    Text(response?.statusCode?.value ?: "", Modifier.padding(end = 15.dp))
                    if (response?.version == HttpVersion.HTTP_1_1) {
                        Icon(
                            modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                            imageVector = Icons.Default.Warning,
                            contentDescription = response?.version?.value,
                            tint = Color.LightGray
                        )
                    }
                    Text(response?.version?.value ?: "")
                }
                Row(
                    Modifier.fillMaxHeight().weight(1f).padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(response?.contentType ?: "")
                }
            }
        },

        scaffoldState = scaffoldState,
        floatingActionButton = {
            Row {
                if (responseVisible && !responseInPopup) {
                    Button(
                        onClick = {
                            responseInPopup = true
                            thread {
                                scope.launch {
                                    delay(100)
                                    justTry {
                                        responseFocusRequest.requestFocus()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in a popup",
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        lastRequestId += 1
                        if (!requesting) {
                            scope.launch { responseVisible = true }
                            thread {
                                response = null
                                requesting = true
                                responseProgress = "Sending request..."
                                val useVars = RequestService.separateVars(vars, varGroup)
                                response = RequestService.makeRequest(request, useVars, lastRequestId)
                                scope.launch {
                                    if (lastRequestId == response?.id) {
                                        responseProgress = "Done"
                                        requesting = false
                                        delay(100)
                                        justTry {
                                            responseFocusRequest.requestFocus()
                                        }
                                        Taskbar.getTaskbar().requestUserAttention(true, false)
                                    }
                                }
                            }
                        } else {
                            responseProgress = "Canceled request"
                            requesting = false
                        }
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
                        .alpha(if (!requesting) ContentAlpha.high else ContentAlpha.disabled)
                ) {
                    Icon(
                        imageVector = if (requesting) Icons.Default.Cancel else if (!responseVisible) Icons.Default.Send else Icons.Default.Refresh,
                        contentDescription = if (requesting) "Cancel" else if (!responseVisible) "Execute" else "Refresh",
                    )
                }
            }
        },

        drawerGesturesEnabled = false,
        drawerBackgroundColor = Color.Transparent,
        drawerContentColor = Color.Transparent,
        drawerElevation = 0.dp,
        drawerContent = {
            scaffoldState.drawerState.DrawerRequestList(
                scope,
                requests,
                vars,
                varGroup,
                ::loadRequest,
                ::selectVarGroup,
                ::newRequest,
                ::importRequest,
                ::deleteRequest
            )
        }
    ) {
        Column(modifier = Modifier.padding(bottom = 56.dp)) {
            Surface(elevation = 1.dp) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.width(120.dp)) {
                        DropDown(
                            Method.values().map(Method::value).toSet(),
                            request.method.value,
                            Modifier.height(56.dp)
                        ) {
                            onChangeRequest(request.copy(method = Method.find(it) ?: Method.GET))
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = url,
                            singleLine = true,
                            onValueChange = {
                                url = it.copy(annotatedString = annotatedStr(it.text, CodeLang.Param))
                                if (request.url != it.text) {
                                    onChangeRequest(request.copy(url = it.text))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .focusRequester(urlFocusRequest)
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f, true)) {
                Surface(elevation = 1.dp) {
                    TabRow(selectedTabIndex = requestTabItemsSelected.ordinal) {
                        TabRequestItems.values().forEach {
                            Tab(
                                text = { Text(it.title, fontWeight = FontWeight.Normal) },
                                selected = requestTabItemsSelected == it,
                                onClick = {
                                    requestTabItemsSelected = it
                                })
                        }
                    }
                }
                when (requestTabItemsSelected) {
                    TabRequestItems.HEADERS ->
                        KeyValueTable(LinkedList(request.headers), allowDisable = true) {
                            onChangeRequest(request.copy(headers = it))
                        }

                    TabRequestItems.DATA -> {
                        RequestData(request, ::onChangeRequest)
                    }

                    TabRequestItems.QUERY_PARAMS ->
                        KeyValueTable(LinkedList(request.queryParams)) {
                            val newRequest = request.copy(queryParams = it)
                            url = url.copy(annotatedString = annotatedStr(newRequest.url, CodeLang.Param))
                            onChangeRequest(newRequest)
                        }
                }
            }
            if (!responseInPopup) {
                AnimatedVisibility(
                    responseVisible,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut(animationSpec = tween(50)),
                    modifier = Modifier.weight(2f, true)
                ) {
                    ResponsePanel(request, response, responseProgress, responseFocusRequest)
                }
            } else {
                val rememberWindowState = rememberPrefWindowState("resp")

                Window(onCloseRequest = { responseInPopup = false }, state = rememberWindowState, title = "Response") {
                    onClose {
                        savePrefWindowState("resp", window)
                    }
                    MaterialAppearance {
                        Box(Modifier.padding(top = 25.dp)) {
                            ResponsePanel(request, response, responseProgress, responseFocusRequest)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestData(req: Request, onChangeRequest: (Request) -> Unit) {
    var requestFieldValue by remember(req) {
        mutableStateOf(
            TextFieldValue(annotatedString = annotatedStr(req.data ?: "", CodeLang.Json))
        )
    }

    OutlinedTextField(
        value = requestFieldValue,
        onValueChange = {
            requestFieldValue = it.copy(annotatedString = annotatedStr(it.text, CodeLang.Json))
            if (req.data != it.text) {
                onChangeRequest(req.copy(data = it.text))
            }
        },
        label = { Text("body") },
        textStyle = codeStyleSource,
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)

    )
}

fun justTry(function: () -> Unit) {
    try {
        function()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

enum class TabRequestItems(val title: String) {
    QUERY_PARAMS("Query Params"),
    HEADERS("Header"),
    DATA("Data")
}

fun annotatedStr(response: Response?, pg: String): AnnotatedString {
    return response?.let {
        if (it.statusCode != null) {
            val status = annotatedStr(it.statusLineString(), CodeLang.Http)
            val separator = annotatedStr(System.lineSeparator())
            val headers = annotatedStr(it.headerLineString())

            status + separator + separator + headers
        } else {
            null
        }
    } ?: annotatedStr(pg)
}

fun annotatedStr(code: String, codeLang: CodeLang = CodeLang.Header): AnnotatedString {
    return parseCodeAsAnnotatedString(
        parser = PrettifyParser(),
        theme = CodeThemeType.Material.theme,
        lang = codeLang,
        code = code
    )
}

fun defaultTabRequest(request: Request): TabRequestItems {
    return if (request.method in listOf(Method.POST, Method.PUT)) {
        TabRequestItems.DATA
    } else {
        TabRequestItems.QUERY_PARAMS
    }
}

@Composable
fun coloringMethods(method: Method): Color {
    return when (method) {
        Method.GET -> Color(0xFF259B49)
        Method.POST -> Color(0xFFD19607)
        Method.PUT -> Color(0xFF358DF1)
        Method.DELETE -> Color(0xFFE54C41)
        else -> MaterialTheme.colors.onPrimary
    }
}