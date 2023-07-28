package com.orbitasolutions.geleia.domains

data class Response(
    val id: Int? = null,
    val version: HttpVersion? = null,
    val statusCode: HttpCodes? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val contentType: String? = null,
    val data: String? = null
) {
    fun statusLineString() = "${version?.value} ${statusCode?.code} ${statusCode?.value}"
    fun headerLineString() = headers.map { "${it.key}: ${it.value.joinToString()}"}.joinToString(System.lineSeparator())

    override fun toString(): String {
        return buildString {
            appendLine("${version?.value ?: ""} ${statusCode?.code ?: ""} ${statusCode?.value ?: ""}")
            appendLine()
            headers.forEach {
                appendLine("${it.key}: ${it.value.joinToString()}")
            }
            appendLine()
            appendLine()
            appendLine(data ?: "")
        }
    }
}