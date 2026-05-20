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
class GetRequestControllerTests {

    @Test
    fun getRequestController() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/request?query=query&Queries=queries1,queries2"))
            .header("header", "header")
            .header("Headers", "1")
            .header("Headers", "2")
            .timeout(Duration.ofSeconds(5))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        assertEquals(
            "Path: /request, Query: query, Queries: [queries1,queries2], Header: header, Headers: [1, 2]",
            response.body()
        )
    }

    @Test
    fun getRequestControllerOptionalMissingSuccess() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/request"))
            .timeout(Duration.ofSeconds(5))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("Path: /request, Query: null, Queries: null, Header: null, Headers: null", response.body())
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
