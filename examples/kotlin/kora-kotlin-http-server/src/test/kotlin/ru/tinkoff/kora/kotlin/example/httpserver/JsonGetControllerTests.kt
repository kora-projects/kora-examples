package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class JsonGetControllerTests {
    @Test
    fun jsonGetController() {
        val response = container.get("/json")
        assertEquals(200, response.statusCode(), response.body())
        JSONAssert.assertEquals("""{"greeting":"Hello world"}""", response.body(), JSONCompareMode.STRICT)
    }

    @Test
    fun jsonEntityGetController() {
        val response = container.get("/json/entity")
        assertEquals(201, response.statusCode(), response.body())
        JSONAssert.assertEquals("""{"greeting":"Hello world"}""", response.body(), JSONCompareMode.STRICT)
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
