package com.orbitasolutions.geleia.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    placeholderText: String = "",
    font: TextStyle = MaterialTheme.typography.subtitle2,
    onValueChange: (String) -> Unit,
) {
    val defaultBorder = BorderStroke(1.dp, TextFieldDefaults.textFieldColors().trailingIconColor(false, false).value)
    val activeBorder = BorderStroke(2.dp, MaterialTheme.colors.primary)
    var border by remember { mutableStateOf(defaultBorder) }

    BasicTextField(modifier = modifier
        .background(
            color = MaterialTheme.colors.surface,
            MaterialTheme.shapes.small
        )
        .border(border, MaterialTheme.shapes.small)
        .padding(horizontal = 16.dp, vertical = 0.dp)
        .fillMaxWidth()
        .onFocusChanged {
            border = (if (it.hasFocus) activeBorder else defaultBorder)
        },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = font.copy(
            color = MaterialTheme.colors.onSurface
        ),
        decorationBox = { innerTextField ->
            Row(modifier, verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) leadingIcon()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty())
                        Text(
                            placeholderText,
                            style = font.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
                        )
                    innerTextField()
                }
                if (trailingIcon != null) trailingIcon()
            }
        }
    )
}