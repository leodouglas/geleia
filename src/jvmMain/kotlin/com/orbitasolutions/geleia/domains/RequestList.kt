package com.orbitasolutions.geleia.domains

import kotlin.random.Random

class RequestList(list: List<Request>) : ArrayList<Request>(list) {

    var hashCode = Random.nextInt()

    override fun equals(other: Any?): Boolean {
        other as RequestList
        return hashCode == other.hashCode
    }

    override fun hashCode() = hashCode

    override fun set(index: Int, element: Request): Request {
        hashCode = Random.nextInt()
        return super.set(index, element)
    }

    override fun add(element: Request): Boolean {
        hashCode = Random.nextInt()
        return super.add(element)
    }

    override fun addAll(elements: Collection<Request>): Boolean {
        hashCode = Random.nextInt()
        return super.addAll(elements)
    }

    override fun remove(element: Request): Boolean {
        hashCode = Random.nextInt()
        return super.remove(element)
    }
}