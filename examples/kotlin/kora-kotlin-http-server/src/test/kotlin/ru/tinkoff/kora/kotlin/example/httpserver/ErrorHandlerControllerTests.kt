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
class ErrorHandlerControllerTests {

    @Test
    fun errorHandlerController() {
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/intercepted/error/1"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(400, response.statusCode(), response.body())
        JSONAssert.assertEquals(
            """
            {
              "code":"BAD_REQUEST",
              "id":"1",
              "message":"ID can't be less 100"
            }
            """.trimIndent(),
            response.body(),
            JSONCompareMode.STRICT
        )
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
