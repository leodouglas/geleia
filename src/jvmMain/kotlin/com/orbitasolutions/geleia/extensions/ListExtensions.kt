package com.orbitasolutions.geleia.extensions

import com.orbitasolutions.geleia.utils.KeyStringValue

fun List<KeyStringValue>.findOrNull(key: String): String? {
    return this.find { it.key == key }?.value
}