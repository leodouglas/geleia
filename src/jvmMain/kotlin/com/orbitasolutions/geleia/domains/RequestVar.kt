package com.orbitasolutions.geleia.domains

class RequestVar(key: String, value: String, command: String? = null, group: String? = null) {

    val group: String
    val key: String
    val value: String
    val command: String?

    init {
        this.key = key.uppercase().trim()
        this.value = value.trim()
        this.group = group?.uppercase()?.trim() ?: "DEFAULT"
        this.command = command
    }

    override fun toString(): String {
        return "RequestVar(key=$key, value=$value, group=$group)"
    }

    fun isGroup(compare: String) = compare.equals(group, true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as RequestVar

        if (group != other.group) return false
        return key == other.key
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }


}
