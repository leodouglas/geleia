package com.orbitasolutions.geleia

import androidx.compose.ui.window.*
import com.orbitasolutions.geleia.compose.app
import com.orbitasolutions.geleia.extensions.MaterialAppearance
import com.orbitasolutions.geleia.extensions.rememberPrefWindowState
import com.orbitasolutions.geleia.extensions.savePrefWindowState
import java.awt.*
import javax.imageio.ImageIO

class Main {

    fun open() = application {
        val rememberWindowState = rememberPrefWindowState("main")

        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState,
            title = ""
        ) {
            MaterialAppearance {
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