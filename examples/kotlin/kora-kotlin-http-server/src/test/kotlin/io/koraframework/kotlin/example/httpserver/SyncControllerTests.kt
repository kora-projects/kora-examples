package io.koraframework.kotlin.example.httpserver

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SyncControllerTests {
    @Test
    fun syncController() {
        container.assertBody("/sync", 200, "Hello world")
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
