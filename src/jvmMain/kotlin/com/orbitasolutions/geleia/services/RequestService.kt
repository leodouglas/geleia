package com.orbitasolutions.geleia.services

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.orbitasolutions.geleia.domains.*
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.*
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.name


object RequestService {

    private fun loadFileCommands(command: String): MutableList<String> {
        val commands = mutableListOf<String>()

        val resourcePath = "curls.txt"
        val file = File(resourcePath)

        if (file.exists()) {
            var endLine = true
            file.forEachLine {
                var line = it
                if (!endLine) {
                    line = commands.last() + System.lineSeparator() + it
                    commands.removeLast()
                } else {
                    if (it.isBlank() || it.startsWith("#"))
                        return@forEachLine
                }

                if (line.startsWith(command)) {
                    commands.add(line.removeSurrounding(prefix = "", suffix = "\\"))
                    endLine = !(it.endsWith("\\") || (line.count { char -> char == '\'' } % 2 != 0))
                }

            }
        }

        return commands
    }

    fun loadVars(): Set<RequestVar> {
        val systemVars = System.getenv().map {
            RequestVar(it.key, it.value)
        }.toSet()

        val commands = loadFileCommands("var")
        val fileVars = commands.map { command: String ->
            val commandParams = command.split("=")
            if (commandParams.size < 2) return@map null
            val value = commandParams[1].trim().removeSurrounding("'")
            val key = commandParams[0].removePrefix("var").trim()
            key.split(":").let {
                RequestVar(it[0], value, command, it.getOrNull(1))
            }
        }.filterNotNull().also {
            println("Reading vars: $it")
        }.toSet()

        return fileVars + systemVars.filter { !fileVars.contains(it) }
    }

    fun loadFileRequests(): RequestList {
        val commands = loadFileCommands("curl")

        return commands.map(RequestService::convertCurlCommand)
            .let(::RequestList)
            .apply {
                if (this.isEmpty()) this.add(Request(modified = true))
            }
    }

    fun saveFileRequests(vars: Set<RequestVar>, requestList: RequestList) {
        val resourcePath = "curls.txt"
        val file = File(resourcePath)

        file.writeText(buildString {
            vars.mapNotNull(RequestVar::command)
                .onEach {
                    appendLine(it)
                }.let {
                    if (it.isNotEmpty())
                        appendLine()
                }


            requestList.mapNotNull(Request::command)
                .onEach {
                    appendLine(it)
                    appendLine()
                }
        }.trim())
    }

    fun convertCurlCommand(originalCurlCommand: String): Request {
        val curlCommand = originalCurlCommand.replace(System.lineSeparator(), " ")
        val methodRegex = "(-X|--request)\\s+(\\w+)".toRegex()
        val urlRegex = "(https?://\\S*(?<!'))".toRegex()
        val headerRegex = "(-H|--header)\\s+[\"'](.*?)[\"']".toRegex()
        val dataRegex = "(-d|--data-raw|--data)\\s+\'(.*?)\'".toRegex()
        val nameRegex = "#(.*)".toRegex()

        val methodMatch = methodRegex.find(curlCommand)
        val urlMatch = urlRegex.find(curlCommand)
        val nameMatch = nameRegex.find(curlCommand)
        val dataMatch = dataRegex.find(curlCommand)

        val url = urlMatch?.groupValues?.getOrNull(1) ?: ""

        val userDir = System.getProperty("user.dir")
        val path = Paths.get(userDir)
        val suggestedName = "${path.name} - ${URL(url).path?.split("/")?.lastOrNull() ?: "untitled"}"
        val name = nameMatch?.groupValues?.getOrNull(1)?.trim() ?: suggestedName

        val headers = mutableMapOf<String, String>()
        headerRegex.findAll(curlCommand).forEach { matchResult ->
            val header = matchResult.groupValues[2]
            val headerParts = header.split(":")
            if (headerParts.size == 2) {
                val key = headerParts[0].trim()
                val value = headerParts[1].trim()
                headers[key] = value
            }
        }


        val contentType = headers["Content-Type"]?.split(";")?.first()?.trim()
        val data = dataMatch?.groupValues?.getOrNull(2)?.let { it.ifEmpty { null } }?.toPretty(contentType)
        val method = methodMatch?.groupValues?.getOrNull(2) ?: if (data != null) "POST" else "GET"

        val request = Request(
            method = Method.find(method) ?: Method.GET,
            headers = headers,
            data = data,
            name = name,
            url = url
        ).also {
            println("Reading curl: $it")
        }

        return request.copy(command = generateCurl(request))
    }

    fun separateVars(vars: Set<RequestVar>, group: String): Map<String, String> {
        val defaultVars = vars.filter { it.isGroup("DEFAULT") }
            .associate { Pair(it.key, it.value) }
        val groupVars = vars.filter { it.isGroup(group) }
            .associate { Pair(it.key, it.value) }

        return defaultVars + groupVars
    }

    private fun applyVars(text: String, vars: Map<String, String>): String {
        var value = text
        vars.asIterable().reversed().onEach {
            value = value.replace("{{${it.key}}}", it.value, true)
        }
        return value
    }

    fun makeRequest(
        request: Request,
        vars: Map<String, String>,
        id: Int? = null,
        forceHTTP1: Boolean = false
    ): Response {
        try {
            val clientRequest = HttpRequest.newBuilder()
            if (forceHTTP1) {
                clientRequest.version(HttpClient.Version.HTTP_1_1)
            }

            clientRequest.uri(URI.create(applyVars(request.url, vars)))
            clientRequest.method(request.method.value, BodyPublishers.ofString(applyVars(request.data ?: "", vars)))

            request.headers.forEach {
                if (it.key.isNotBlank()) {
                    clientRequest.header(applyVars(it.key, vars), applyVars(it.value ?: "", vars))
                }
            }
            clientRequest.header("Cache-Control", "no-cache")
            clientRequest.header("User-Agent", "Geleia/0.1.0:Java/${Runtime.version()}")
            clientRequest.header("Geleia-RequestId", UUID.randomUUID().toString())

            println("Executing: " + clientRequest.build().toString())
            val response = HttpClient.newHttpClient().send(clientRequest.build(), HttpResponse.BodyHandlers.ofString())

            val contentType = response.headers().map()["Content-Type"]?.first()?.split(";")?.first()?.trim()
            val statusCode = HttpCodes.find(response.statusCode())
            val httpVersion = response.version().toPretty()
            val headers = response.headers().map()
            val data = response.body().toPretty(contentType)

            return Response(id, httpVersion, statusCode, headers, contentType, data)
        } catch (ex: IOException) {
            if (ex.stackTraceToString().contains("HTTP/1.1")) {
                return makeRequest(request, vars, id, true)
            }
            return Response(data = ex.stackTraceToString(), id = id)
        } catch (ex: InterruptedException) {
            return Response(data = ex.stackTraceToString(), id = id)
        } catch (ex: Exception) {
            return Response(data = "Could not send request", id = id)
        }

    }

    fun generateCurl(request: Request): String {
        with(request) {
            return buildString {
                append("curl -X $method ${url.ifEmpty { "http://localhost" }}")
                headers.onEach {
                    appendLine(" \\")
                    append("-H \"${it.key}: ${it.value}\"")
                }
                data?.let {
                    appendLine(" \\")
                    append("-d '$it'")
                }
                append(" # $name")
            }
        }

    }

}

private fun String.toPretty(format: String? = "application/json"): String {
    return try {
        when (format?.lowercase()) {
            "application/json" -> {
                val jsonElement = JsonParser.parseString(this)
                val gson = GsonBuilder().setLenient().setPrettyPrinting().create()
                gson.toJson(jsonElement)
            }

            else -> this
        }
    } catch (e: Exception) {
        this
    }
}

private fun HttpClient.Version.toPretty(): HttpVersion {
    return when (this) {
        HTTP_1_1 -> HttpVersion.HTTP_1_1
        HTTP_2 -> HttpVersion.HTTP_2
    }
}
