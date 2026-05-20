package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class MapperResponseControllerTests {
    @Test
    fun mapperResponseController() {
        container.assertBody("/mapper/response/bob", 200, "Hello World - bob")
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
