package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import com.jayway.jsonpath.JsonPath
import com.orbitasolutions.geleia.domains.Request
import com.orbitasolutions.geleia.domains.Response
import com.orbitasolutions.geleia.utils.getProjectPref
import com.orbitasolutions.geleia.utils.setProjectPref
import com.wakaztahir.codeeditor.model.CodeLang
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import kotlin.concurrent.thread

@Composable
fun ResponsePanel(
    req: Request,
    resp: Response?,
    progress: String,
    responseFocusRequester: FocusRequester
) {
    var showFilterPanel by remember { mutableStateOf(false) }
    var findPath by remember { mutableStateOf(getProjectPref("request.${req.id}.filter", "$")) }
    var usingFindPath by remember { mutableStateOf(false) }
    var findText: String by remember { mutableStateOf("") }
    var findNext by remember { mutableStateOf(0) }
    val inputFilterFocusRequester = remember { FocusRequester() }
    val filterFocusRequester = remember { FocusRequester() }

    var filteredJson by remember(resp, progress) { mutableStateOf("") }

    fun annotatedResponse() = if (filteredJson.isNotEmpty()) {
        annotatedStr(filteredJson, CodeLang.Json)
    } else {
        annotatedStr(resp) ?: annotatedStr(progress)
    }

    var findTextSelected by remember { mutableStateOf(TextRange.Zero) }
    var findError by remember { mutableStateOf(false) }

    var responseFieldValue by remember(resp, progress, filteredJson, findText, findNext) {
        mutableStateOf(TextFieldValue(annotatedString = annotatedResponse(), selection = findTextSelected))
    }

    fun filter() {
        val json = resp?.data ?: return
        try {
            val o = JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(json)
            val jsonpath = if (o is JSONObject) {
                JsonPath.read<JSONObject>(o, findPath)
            } else {
                JsonPath.read<JSONArray>(o, findPath)
            }
            filteredJson = GsonBuilder().setLenient()
                .setPrettyPrinting()
                .create()
                .toJson(jsonpath)

            setProjectPref("request.${req.id}.filter", findPath)
            findError = false
        } catch (ex: Exception) {
            filteredJson = ""
            findError = true
        }
        inputFilterFocusRequester.requestFocus()
    }

    @Throws(IndexOutOfBoundsException::class)
    fun calcFindText(first: Boolean = false) {
        if (first) findNext = 0 else findNext += 1
        try {
            if (findText.isEmpty()) {
                findTextSelected = TextRange.Zero
                return
            }
            var index = 0
            var startIn = 0
            repeat(findNext + 1) {
                if (responseFieldValue.text.indexOf(findText, startIn) > 0) {
                    index = responseFieldValue.text.indexOf(findText, startIn)
                    startIn = index + findText.length
                } else {
                    throw IndexOutOfBoundsException(findNext + 1)
                }
            }
            findTextSelected = TextRange(index, index + findText.length)
            findError = false
        } catch (ex: IndexOutOfBoundsException) {
            if (findNext == 0) {
                findError = true
            }
            findNext = 0
            findTextSelected = TextRange.Zero
        }
    }

    fun submitFind() {
        filterFocusRequester.requestFocus()
        if (usingFindPath) {
            filter()
        } else {
            inputFilterFocusRequester.requestFocus()
        }
        calcFindText()
    }

    fun showFind() {
        usingFindPath = false
        findError = false
        showFilterPanel = true
        findNext = 0
        thread {
            Thread.sleep(100)
            justTry {
                inputFilterFocusRequester.requestFocus()
            }
        }
    }

    fun closeFind() {
        filteredJson = ""
        showFilterPanel = false
        responseFocusRequester.requestFocus()
    }

    OutlinedTextField(
        label = { Text("response") },
        value = responseFieldValue,
        readOnly = true,
        onValueChange = {
            responseFieldValue = it.copy(annotatedString = annotatedResponse())
        },
        textStyle = codeStyleSource,
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
            .focusRequester(responseFocusRequester)
            .onKeyEvent {
                if (it.isReleasedEvent) {
                    if (it.key.nativeKeyCode == KeyEvent.VK_F && it.modifiers == InputEvent.META_DOWN_MASK) {
                        showFind()
                    }
                }
                false
            }
    )

    resp?.data?.let {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(25.dp)) {
            if (showFilterPanel) {
                Surface(elevation = 1.dp, shape = MaterialTheme.shapes.small) {
                    Row(Modifier.height(40.dp).padding(4.dp)) {
                        CustomTextField(
                            id = if (usingFindPath) "findPath" else "findText",
                            error = findError,
                            modifier = Modifier.width(400.dp).focusRequester(inputFilterFocusRequester)
                                .onKeyEvent {
                                    if (it.isReleasedEvent) {
                                        if (it.key.nativeKeyCode == KeyEvent.VK_F3) {
                                            submitFind()
                                        }
                                        if (it.key.nativeKeyCode == KeyEvent.VK_ESCAPE) {
                                            closeFind()
                                        }
                                    }
                                    false
                                },
                            onSubmit = { submitFind() },
                            value = if (usingFindPath) findPath else findText,
                            placeholder = if (usingFindPath) "Find json path" else "Find text",
                            onChange = {
                                findError = false

                                if (usingFindPath) {
                                    findPath = it
                                } else {
                                    findText = it
                                    calcFindText(true)
                                }
                            }
                        )
                        IconButton(modifier = Modifier.size(38.dp).focusRequester(filterFocusRequester),
                            onClick = {
                                usingFindPath = !usingFindPath
                                findText = ""
                                filteredJson = ""
                                findError = false
                                inputFilterFocusRequester.requestFocus()
                            }) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "FindPath",
                                tint = if (usingFindPath) MaterialTheme.colors.primary else MaterialTheme.colors.onPrimary
                            )
                        }
                        val uriHandler = LocalUriHandler.current
                        IconButton(modifier = Modifier.size(38.dp), enabled = usingFindPath,
                            onClick = {
                                uriHandler.openUri("https://support.smartbear.com/alertsite/docs/monitors/api/endpoint/jsonpath.html")
                            }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "info",
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(modifier = Modifier.size(38.dp),
                onClick = {
                    if (!showFilterPanel) {
                        showFind()
                    } else {
                        closeFind()
                    }
                }) {
                Icon(
                    imageVector = if (showFilterPanel) Icons.Default.Close else Icons.Default.FilterList,
                    contentDescription = "Filter"
                )
            }

        }
    }
}