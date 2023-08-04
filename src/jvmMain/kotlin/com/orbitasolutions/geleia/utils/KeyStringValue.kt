package com.orbitasolutions.geleia.utils

import kotlin.random.Random

data class KeyStringValue(val key: String, val value: String = "", val disabled: Boolean = false, val index: Int = Random.nextInt())
