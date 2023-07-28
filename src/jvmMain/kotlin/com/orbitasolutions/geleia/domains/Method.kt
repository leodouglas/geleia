package com.orbitasolutions.geleia.domains

enum class Method(val value: String, val short: String) {
    GET("GET", "GET"),
    POST("POST", "POST"),
    PUT("PUT", "PUT"),
    PATCH("PATCH", "PAT"),
    DELETE("DELETE", "DEL"),
    COPY("COPY", "COPY"),
    HEAD("HEAD", "HEAD"),
    OPTIONS("OPTIONS", "OPT"),
    LINK("LINK", "LINK"),
    UNLINK("UNLINK", "ULNK"),
    PURGE("PURGE", "PURG"),
    LOCK("LOCK", "LOCK"),
    UNLOCK("UNLOCK", "ULCK"),
    PROPFIND("PROPFIND", "PROP"),
    VIEW("VIEW", "VIEW");

    companion object {
        fun find(value: String): Method? {
            return Method.values().find { it.value == value }
        }
        fun findByShort(short: String): Method? {
            return Method.values().find { it.short == short }
        }
    }
}