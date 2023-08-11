package com.orbitasolutions.geleia.extensions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.orbitasolutions.geleia.theme.jetbrainsDarkColors
import com.orbitasolutions.geleia.theme.jetbrainsTypo
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.prefs.Preferences
import javax.swing.JRootPane

@Composable
fun DialogWindowScope.MaterialAppearance(content: @Composable (ColumnScope) -> Unit) {
    this.window.rootPane.MaterialAppearance {
        WindowDraggableArea {
            Column(Modifier.fillMaxWidth().padding(10.dp).padding(top = 20.dp)) {
                content(this)
            }
        }
    }
}

@Composable
fun FrameWindowScope.MaterialAppearance(content: @Composable () -> Unit) {
    this.window.rootPane.MaterialAppearance { content() }
}

@Composable
fun JRootPane.MaterialAppearance(content: @Composable () -> Unit) {
    putClientProperty("apple.awt.fullWindowContent", true)
    putClientProperty("apple.awt.transparentTitleBar", true)
    putClientProperty("apple.awt.windowTitleVisible", false)

    putClientProperty("jetbrains.awt.windowDarkAppearance", true)
    putClientProperty("jetbrains.awt.transparentTitleBarAppearance", true)
    MaterialTheme(colors = jetbrainsDarkColors(), typography = jetbrainsTypo()) {
        Surface(color = MaterialTheme.colors.surface, contentColor = contentColorFor(MaterialTheme.colors.background)) {
            content()
        }
    }
}

@Composable
fun rememberPrefWindowState(alias: String): WindowState {
    val prefs = Preferences.userRoot()
    val screenSize = Toolkit.getDefaultToolkit().screenSize

    return rememberWindowState(width = prefs.getInt("window.$alias.width", screenSize.height).dp,
        height = prefs.getInt("window.$alias.height", (screenSize.height * 0.8).toInt()).dp,
        position = WindowPosition(
            prefs.getInt("window.$alias.x", WindowPosition.PlatformDefault.x.value.toInt()).dp,
            prefs.getInt("window.$alias.y", WindowPosition.PlatformDefault.y.value.toInt()).dp
        ))

}

fun savePrefWindowState(alias: String, window: Window) {
    val prefs = Preferences.userRoot()
    window.location.let { location ->
        prefs.putInt("window.$alias.x", location.x)
        prefs.putInt("window.$alias.y", location.y)
    }
    window.size?.let { dimension ->
        prefs.putInt("window.$alias.width", dimension.width)
        prefs.putInt("window.$alias.height", dimension.height)
    }
}

fun WindowScope.onClose(onClosing: (WindowEvent) -> Unit){
    this.window.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
            onClosing(e)
        }
    })
}