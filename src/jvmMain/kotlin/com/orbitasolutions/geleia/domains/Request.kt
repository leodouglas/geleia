package com.orbitasolutions.geleia.domains

import com.orbitasolutions.geleia.utils.KeyStringValue
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.random.Random

data class Request(
    val id: Int = Random.nextInt(),
    val name: String = "untitled",
    val method: Method = Method.GET,
    val headers: List<KeyStringValue> = listOf(),
    val data: String? = null,
    val protocol: String = "http",
    val host: String = "",
    val path: String = "",
    val port: Int = 80,
    val queryParams: List<KeyStringValue> = listOf(),
    val modified: Boolean = false,
    val command: String? = null
) {

    constructor(
        id: Int = Random.nextInt(),
        name: String = "untitled",
        method: Method = Method.GET,
        headers: List<KeyStringValue> = listOf(),
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
        host = url.tryUrl()?.host ?: "localhost",
        port = url.tryUrl()?.port.takeIf { it != -1 } ?: 80,
        protocol = url.tryUrl()?.protocol ?: "http",
        path = url.tryUrl()?.path?.trim() ?: "",
        queryParams = setQueryParams(url.tryUrl()?.query ?: ""),
        modified = modified,
        command = command
    )

    val url: String
        get() {
            val queryString = queryParams.let { params ->
                params.filter { it.key.isNotBlank() }.joinToString("&") {
                    "${URLEncoder.encode(it.key, Charset.defaultCharset())}=${
                        URLEncoder.encode(
                            it.value,
                            Charset.defaultCharset()
                        )
                    }"
                }
            }

            return buildString {
                append("$protocol://$host")
                if (port != 80) append(":$port")
                append(path)
                if (queryString.isNotEmpty()) append("?$queryString")
            }.trim()
        }

    companion object {
        private fun setQueryParams(url: String): List<KeyStringValue> {
            return url.split("&").filter(String::isNotBlank).map {
                val entry = it.split("=")
                val key = URLDecoder.decode(entry[0], Charset.defaultCharset())
                val value = URLDecoder.decode(entry.getOrNull(1) ?: "", Charset.defaultCharset())
                KeyStringValue(key, value)
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
private fun String.tryUrl(): URL? {
    return try {
        URL(this)
    } catch (ex: Exception) {
        null
    }
}