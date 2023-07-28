package com.orbitasolutions.geleia.theme

import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color

fun jetbrainsDarkColors(
    background: Color = Color(0xFF1E2022),
    primary: Color = Color(0xFF3574F0),
    secondary: Color = Color(0xFF57965C),
    primaryVariant: Color = Color(0xFF90B3F7),
    secondaryVariant: Color = Color(0xFF91B859),
    surface: Color = Color(0xFF1E2022),
    error: Color = Color(0xFFCF6679),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onBackground: Color = Color.White,
    onSurface: Color = Color.White,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun jetbrainsTypo() = Typography().copy(

)