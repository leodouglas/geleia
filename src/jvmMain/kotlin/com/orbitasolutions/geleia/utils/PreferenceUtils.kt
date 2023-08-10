package com.orbitasolutions.geleia.utils

import java.nio.file.Paths
import java.util.prefs.Preferences
import kotlin.io.path.name

fun getProjectPref(prop: String, defaultValue: String): String {
    val prefs = Preferences.userRoot()

    val userDir = System.getProperty("user.dir")
    val path = Paths.get(userDir)

    return prefs.get("${path.name}.$prop", defaultValue) ?: defaultValue
}

fun setProjectPref(prop: String, value: String) {
    val prefs = Preferences.userRoot()
    val userDir = System.getProperty("user.dir")
    val path = Paths.get(userDir)

    prefs.put("${path.name}.$prop", value)
}