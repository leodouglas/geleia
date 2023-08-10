package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.wakaztahir.codeeditor.model.CodeLang
import java.awt.event.KeyEvent.*

@Composable
fun CustomTextField(
    id: String,
    value: String,
    modifier: Modifier = Modifier,
    codeLang: CodeLang = CodeLang.Param,
    font: TextStyle = MaterialTheme.typography.subtitle2,
    onChange: (String) -> Unit,
    placeholder: String = "",
    error: Boolean = false,
    onSubmit: (String) -> Unit = {}
) {
    val defaultBorder = BorderStroke(1.dp, TextFieldDefaults.textFieldColors()
        .trailingIconColor(enabled = false, isError = false).value)
    val activeBorder = BorderStroke(2.dp, MaterialTheme.colors.primary)
    val errorBorder = BorderStroke(2.dp, MaterialTheme.colors.error)
    var border by remember { mutableStateOf(defaultBorder) }
    var textFieldValue by remember(id.hashCode()) {
        mutableStateOf(TextFieldValue(annotatedString = annotatedStr(value, codeLang)))
    }

    BasicTextField(
        onValueChange = {
            textFieldValue = it.copy(annotatedString = annotatedStr(it.text, codeLang))
            if (value != it.text) {
                onChange(it.text)
            }
        },
        modifier = modifier.background(color = MaterialTheme.colors.surface, MaterialTheme.shapes.small)
            .border( if (error) errorBorder else border , MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .onFocusChanged {
                border = if (it.hasFocus) activeBorder else defaultBorder
                //TEST:
                if (it.isFocused) {
                    textFieldValue = textFieldValue.copy(selection = TextRange(0, value.length))
                }
            }
            .onKeyEvent {
                if (it.isReleasedEvent && it.key.nativeKeyCode == VK_ENTER) {
                    onSubmit(value)
                }
                false
            }
            .height(38.dp),
        value = textFieldValue,

        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = font.copy(color = MaterialTheme.colors.onSurface),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (textFieldValue.text.isEmpty())
                    Text(placeholder,
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            fontSize = font.fontSize
                        )
                    )
                innerTextField()
            }
        }
    )
}

val KeyEvent.isReleasedEvent: Boolean
    get() = awtEventOrNull?.id == KEY_RELEASED

val KeyEvent.modifiers: Int?
    get() = awtEventOrNull?.modifiersEx