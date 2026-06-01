package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Testcontainers
class GetParametersControllerTests {

    @Test
    fun getParametersController() {
        val response = send("/parameters/path?query=query&Queries=queries1,queries2") {
            header("header", "header")
            header("Headers", "1")
            header("Headers", "2")
        }
        assertEquals(200, response.statusCode(), response.body())
        assertEquals(
            "Path: path, Query: query, Queries: [queries1,queries2], Header: header, Headers: [1, 2]",
            response.body()
        )
    }

    @Test
    fun getParametersControllerOptionalMissingSuccess() {
        val response = send("/parameters/path")
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("Path: path, Query: null, Queries: null, Header: null, Headers: null", response.body())
    }

    private fun send(path: String, customize: HttpRequest.Builder.() -> Unit = {}): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve(path))
            .timeout(Duration.ofSeconds(5))
            .apply(customize)
            .build()
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
