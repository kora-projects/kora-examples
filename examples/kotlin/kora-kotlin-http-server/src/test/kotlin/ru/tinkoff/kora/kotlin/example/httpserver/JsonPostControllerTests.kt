package ru.tinkoff.kora.kotlin.example.httpserver

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
class JsonPostControllerTests {
    @Test
    fun jsonPostController() {
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString("""{"id":"1"}"""))
            .uri(container.getURI().resolve("/json"))
            .timeout(Duration.ofSeconds(5))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        JSONAssert.assertEquals("""{"name":"Ivan","value":100}""", response.body(), JSONCompareMode.STRICT)
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
