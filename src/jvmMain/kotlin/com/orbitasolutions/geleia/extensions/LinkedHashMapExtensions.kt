package com.orbitasolutions.geleia.extensions

fun <K, V> LinkedHashMap<K, V>.insertAtIndex(key: K, value: V, index: Int) {
    if (index < 0) {
        throw IllegalArgumentException("Cannot insert into negative index")
    }
    if (index > size) {
        put(key, value)
        return
    }
    val holderMap = LinkedHashMap<K, V>(size + 1)
    entries.forEachIndexed { i, entry ->
        if (i == index) {
            holderMap[key] = value
        }
        holderMap[entry.key] = entry.value
    }
    clear()
    putAll(holderMap)
}