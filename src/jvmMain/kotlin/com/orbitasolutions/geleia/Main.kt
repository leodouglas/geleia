package com.orbitasolutions.geleia

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.orbitasolutions.geleia.compose.app
import com.orbitasolutions.geleia.theme.jetbrainsDarkColors
import com.orbitasolutions.geleia.theme.jetbrainsTypo
import java.awt.*
import java.util.prefs.Preferences
import javax.imageio.ImageIO


class Main {

    fun open() = application {
        val prefs = Preferences.userRoot()
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val rememberWindowState = rememberWindowState(
            width = prefs.getInt("window.width", screenSize.height).dp,
            height = prefs.getInt("window.height", (screenSize.height * 0.8).toInt()).dp,
            position = WindowPosition(
                prefs.getInt("window.x", WindowPosition.PlatformDefault.x.value.toInt()).dp,
                prefs.getInt("window.y", WindowPosition.PlatformDefault.y.value.toInt()).dp
            )
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState,
            title = ""
        ) {
            with(window.rootPane) {
                putClientProperty("apple.awt.fullWindowContent", true)
                putClientProperty("apple.awt.transparentTitleBar", true)
                putClientProperty("apple.awt.windowTitleVisible", false)

                putClientProperty("jetbrains.awt.windowDarkAppearance", true)
                putClientProperty("jetbrains.awt.transparentTitleBarAppearance", true)
            }
            MaterialTheme(colors = jetbrainsDarkColors(), typography = jetbrainsTypo()) {
                app {
                    it?.window?.let { window ->
                        window.location.let { location ->
                            prefs.putInt("window.x", location.x)
                            prefs.putInt("window.y", location.y)
                        }
                        window.size?.let { dimension ->
                            prefs.putInt("window.width", dimension.width)
                            prefs.putInt("window.height", dimension.height)
                        }
                    }
                }
            }
        }
    }

    init {
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        System.setProperty("apple.awt.application.appearance", "system")
        System.setProperty("apple.awt.application.name", "Geleia")

        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,host")

        try {
            val image = ImageIO.read(this.javaClass.getResource("/icons/icon.png"))
            with(Taskbar.getTaskbar()) {
                iconImage = image
            }
        } catch (_: Exception) {
        }
    }
}

fun main() {
    Main().open()
}