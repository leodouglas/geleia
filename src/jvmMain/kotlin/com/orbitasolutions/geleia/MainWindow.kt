package com.orbitasolutions.geleia

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.*
import com.orbitasolutions.geleia.compose.app
import com.orbitasolutions.geleia.extensions.MaterialAppearance
import com.orbitasolutions.geleia.extensions.rememberPrefWindowState
import com.orbitasolutions.geleia.services.RequestService
import java.awt.*
import java.io.File
import javax.imageio.ImageIO

class MainWindow(val onClose: () -> Unit = {  }, val windowInst: (ComposeWindow) -> Unit = {}, plugin: Boolean = false) {

    fun open(basePath: File? = null) = application(false) {
        RequestService.basePath = basePath

        val rememberWindowState = rememberPrefWindowState("main")
        var isOpen by remember { mutableStateOf(true) }

        if (isOpen) {
            Window(
                onCloseRequest = { isOpen = false; onClose() },
                state = rememberWindowState,
                title = ""
            ) {
                MaterialAppearance {
                    windowInst(window)
                    app()
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
            if (!plugin) {
                val image = ImageIO.read(this.javaClass.getResource("/icons/icon.png"))
                with(Taskbar.getTaskbar()) {
                    iconImage = image
                }
            }
        } catch (_: Exception) {
        }
    }
}

fun main() {
    MainWindow().open()
}