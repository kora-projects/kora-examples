package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Testcontainers
class InterceptedControllerTests {

    @Test
    fun interceptedController() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/intercepted"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("Hello world", response.body())

        val interceptedLogs = Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .until(
                { container.logs.split("\n") },
                { logs -> logs.any { it.endsWith("Method Level Interceptor") } }
            )
        assertTrue(interceptedLogs.any { it.endsWith("Server Level Interceptor") })
        assertTrue(interceptedLogs.any { it.endsWith("Controller Level Interceptor") })
        assertTrue(interceptedLogs.any { it.endsWith("Method Level Interceptor") })
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
