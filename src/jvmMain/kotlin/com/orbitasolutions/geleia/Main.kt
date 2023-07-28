package com.orbitasolutions.geleia

import androidx.compose.material.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.orbitasolutions.geleia.compose.app
import com.orbitasolutions.geleia.theme.jetbrainsDarkColors
import com.orbitasolutions.geleia.theme.jetbrainsTypo
import java.awt.*
import java.io.File
import javax.imageio.ImageIO


class Main {

    fun open() = application {

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val rememberWindowState = rememberWindowState(
            size = DpSize((screenSize.height).dp, (screenSize.height * 0.8).dp)
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
                app()
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