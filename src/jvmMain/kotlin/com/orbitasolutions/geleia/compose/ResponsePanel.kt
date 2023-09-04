package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ResponseData(
        annotatedResponse: AnnotatedString,
        findTextSelected: TextRange,
        responseFocusRequester: FocusRequester,
        showFind: () -> Unit
) {
    var responseFieldValue by remember(annotatedResponse, findTextSelected) {
        mutableStateOf(TextFieldValue(annotatedString = annotatedResponse, selection = findTextSelected))
    }

    val verticalScrollState = rememberScrollState()

    BasicTextField(
            value = responseFieldValue,
            readOnly = true,
            onValueChange = {
                responseFieldValue = it.copy(annotatedString = annotatedResponse)
            },
            textStyle = codeStyleSource,
            modifier = Modifier.fillMaxSize()
                    .horizontalScroll(verticalScrollState)
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
}

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

    var findTextSelected by remember { mutableStateOf(TextRange.Zero) }
    var findError by remember { mutableStateOf(false) }

    val annotatedResponse = if (filteredJson.isNotEmpty()) {
        annotatedStr(filteredJson, CodeLang.Json)
    } else {
        annotatedStr(resp) ?: annotatedStr(progress)
    }

    val dataLines by remember(resp, progress, filteredJson) {
        mutableStateOf(if (filteredJson.isNotEmpty()) {
            filteredJson.lines()
        } else {
            resp?.data?.lines() ?: listOf()
        })
    }

    val countHeaderLines = annotatedResponse.text.lines().count() - dataLines.count()

    var capsules by remember(resp) {
        mutableStateOf(dataLines.mapIndexed { index, s ->
            if (s.endsWith(CurlyCapsuled.BEGIN_CHAR)) createCurlyCapsule(index, dataLines) else
                if (s.endsWith(SquareCapsuled.BEGIN_CHAR)) createSquareCapsule(index, dataLines) else null
        })
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

    fun capsuleText(capsuled: Capsuled) {
        val capsuledLine = dataLines[capsuled.beginLine].trimEnd() + "..." + dataLines[capsuled.endLine].trimStart()

        val lines = dataLines.mapIndexed { index, s ->
            if (index == capsuled.beginLine) capsuledLine else s
        }.filterIndexed { index, _ ->
            index <= capsuled.beginLine || index > capsuled.endLine
        }

        filteredJson = lines.joinToString(System.lineSeparator())
    }

    @Throws(IndexOutOfBoundsException::class)
    fun calcFindText(text: String, first: Boolean = false) {
        if (first) findNext = 0 else findNext += 1
        try {
            if (findText.isEmpty()) {
                findTextSelected = TextRange.Zero
                return
            }
            var index = 0
            var startIn = 0
            repeat(findNext + 1) {
                if (text.indexOf(findText, startIn) > 0) {
                    index = text.indexOf(findText, startIn)
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
        calcFindText(annotatedResponse.text)
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

    Column(Modifier.padding(8.dp)) {
        val scrollState = rememberScrollState()

        val caretStyleSource = TextStyle(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Thin
        )

        Row(Modifier.verticalScroll(scrollState)) {
            Row {
                buildString {
                    repeat(countHeaderLines) {
                        appendLine()
                    }
                    repeat(dataLines.count()) {
                        appendLine(it.inc())
                    }
                }.let {
                    Text(
                            it,
                            style = codeStyleSource,
                            modifier = Modifier.padding(end = 10.dp).alpha(ContentAlpha.disabled)
                    )
                }
                Column(Modifier.width(18.dp)) {

                    repeat(countHeaderLines) {
                        Text(" ", style = caretStyleSource)
                    }

                    capsules.forEach { capsuled ->
                        capsuled?.let {
                            if (!capsuled.closed) {
                                Text("▼", style = caretStyleSource, modifier = Modifier.alpha(ContentAlpha.disabled)
                                        .clickable {
                                            capsuled.closed = true
                                            capsules = ArrayList(capsules)
                                            capsuleText(capsuled)
                                        })
                            } else {
                                Text("►", style = caretStyleSource, modifier = Modifier.alpha(ContentAlpha.disabled)
                                    .clickable {
                                        capsuled.closed = false
                                        capsules = ArrayList(capsules)
                                    })
                                Text(" ", style = caretStyleSource)
                            }
                        } ?: Text(" ", style = caretStyleSource)
                    }
                }
            }
            Column(Modifier.padding(end = 8.dp)) {
                ResponseData(annotatedResponse, findTextSelected, responseFocusRequester, ::showFind)
            }
        }
    }

    resp?.data?.let {
        Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(end = 20.dp, top = 10.dp)
        ) {
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
                                        calcFindText(annotatedResponse.text, true)
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

fun createCurlyCapsule(start: Int, lines: List<String>): Capsuled? {
    var countSubCapsule = 0
    lines.subList(start, lines.size).forEachIndexed { index, s ->
        if (s.contains(CurlyCapsuled.BEGIN_CHAR)) {
            countSubCapsule += 1
        }
        if (s.contains(CurlyCapsuled.END_CHAR)) {
            if (countSubCapsule > 1) {
                countSubCapsule -= 1
            } else {
                return CurlyCapsuled(false, start, start + index)
            }
        }
    }
    return null
}

fun createSquareCapsule(start: Int, lines: List<String>): Capsuled? {
    var countSubCapsule = 0
    lines.subList(start, lines.size).forEachIndexed { index, s ->
        if (s.contains(SquareCapsuled.BEGIN_CHAR)) {
            countSubCapsule += 1
        }
        if (s.contains(SquareCapsuled.END_CHAR)) {
            if (countSubCapsule > 1) {
                countSubCapsule -= 1
            } else {
                return SquareCapsuled(false, start, start + index)
            }
        }
    }
    return null
}

open class Capsuled(var closed: Boolean, val beginLine: Int, val endLine: Int)

class CurlyCapsuled(closed: Boolean, beginLine: Int, endLine: Int) : Capsuled(closed, beginLine, endLine) {
    companion object {
        const val BEGIN_CHAR = '{'
        const val END_CHAR = '}'
    }
}

class SquareCapsuled(closed: Boolean, beginLine: Int, endLine: Int) : Capsuled(closed, beginLine, endLine) {
    companion object {
        const val BEGIN_CHAR = '['
        const val END_CHAR = ']'
    }
}
