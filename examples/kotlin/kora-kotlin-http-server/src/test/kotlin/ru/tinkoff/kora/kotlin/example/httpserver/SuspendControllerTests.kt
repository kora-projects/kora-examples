package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SuspendControllerTests {
    @Test
    fun suspendController() {
        container.assertBody("/suspend", 200, "Hello world")
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
