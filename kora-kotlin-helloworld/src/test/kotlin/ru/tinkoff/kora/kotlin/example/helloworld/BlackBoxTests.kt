package ru.tinkoff.kora.kotlin.example.helloworld

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration


@Testcontainers
class BlackBoxTests {

    @Container
    private val container = AppContainer.build()

    @Test
    fun helloWorld() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.uri.resolve("/hello/world"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // when
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        // then
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("Hello World", response.body())
    }

    @Test
    fun helloWorldJson() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.uri.resolve("/hello/world/json"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // when
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())

        // then
        JSONAssert.assertEquals("{\"greeting\":\"Hello World\"}", response.body(), JSONCompareMode.STRICT)
    }

}

