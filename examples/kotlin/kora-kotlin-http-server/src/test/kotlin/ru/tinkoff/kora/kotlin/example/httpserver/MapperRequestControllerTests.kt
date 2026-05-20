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
class MapperRequestControllerTests {
    @Test
    fun mapperRequestController() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/mapper/request"))
            .header("x-user-id", "1")
            .header("x-trace-id", "2")
            .timeout(Duration.ofSeconds(5))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("1:2", response.body())
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
