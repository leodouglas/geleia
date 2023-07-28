package com.orbitasolutions.geleia.domains

import java.net.URL
import kotlin.random.Random

data class Request(
    val id: Int = Random.nextInt(),
    val name: String = "untitled",
    val method: Method = Method.GET,
    val headers: Map<String, String?> = mutableMapOf(),
    val data: String? = null,
    val protocol: String = "http",
    val host: String = "",
    val path: String = "",
    val port: Int = 80,
    val queryParams: Map<String, String?> = mutableMapOf(),
    val modified: Boolean = false,
    val command: String? = null
) {

    constructor(
        id: Int = Random.nextInt(),
        name: String = "untitled",
        method: Method = Method.GET,
        headers: Map<String, String?> = mutableMapOf(),
        data: String? = null,
        url: String,
        modified: Boolean = false,
        command: String? = null
    ) : this(
        id = id,
        name = name,
        method = method,
        headers = headers,
        data = data,
        host = URL(url).host ?: "localhost",
        port = URL(url).port.takeIf { it != -1 } ?: 80,
        protocol = URL(url).protocol ?: "http",
        path = URL(url).path.trim(),
        queryParams = setQueryParams(URL(url).query ?: ""),
        modified = modified,
        command = command
    )

    val url: String
        get() {
            val queryString = queryParams.let { params ->
                params.entries.filter { it.key.isNotBlank() }.joinToString("&") { "${it.key}=${it.value}" }
            }

            return buildString {
                append("$protocol://$host")
                if (port != 80) append(":$port")
                append(path)
                if (queryString.isNotEmpty()) append("?$queryString")
            }.trim()
        }

    companion object {
        private fun setQueryParams(url: String): Map<String, String> {
            return url.split("&").filter(String::isNotBlank).associate {
                val entry = it.split("=")
                Pair(entry[0], entry.getOrNull(1) ?: "")
            }.filter { it.key.isNotBlank() }
        }
    }

    fun copy(url: String) = Request(
        id = id,
        name = name,
        method = method,
        headers = headers,
        data = data,
        url = url,
        modified = modified,
        command = command
    )

    override fun hashCode() = id

    override fun equals(other: Any?): Boolean {
        other as Request
        return id == other.id
    }


}