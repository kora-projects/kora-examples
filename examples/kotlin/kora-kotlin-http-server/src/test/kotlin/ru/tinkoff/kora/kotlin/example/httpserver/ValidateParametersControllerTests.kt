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
class ValidateParametersControllerTests {

    @Test
    fun getParametersController() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/validate/path?query=query&Queries=Q1&Queries=Q2"))
            .header("header", "header")
            .header("Headers", "H1")
            .header("Headers", "H2")
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("Path: path, Query: query, Queries: [Q1, Q2], Header: header, Headers: [H1, H2]", response.body())
    }

    @Test
    fun getParametersControllerOptionalMissingSuccess() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/validate/path"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(400, response.statusCode(), response.body())
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
