package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.kotlin.example.http.server.JsonPostController
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(TestApplication::class)
@Testcontainers
class JsonPostControllerIntegrationTests : KoraAppTestConfigModifier {

    @TestComponent
    lateinit var jsonHttpClient: TestApplication.JsonHttpClient

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        testHttpClient {
          url = ${'$'}{HTTP_CLIENT_URL}
          requestTimeout = 10s
          telemetry.logging.enabled = true
        }
        """.trimIndent()
    ).withSystemProperty("HTTP_CLIENT_URL", container.getURI().toString())

    @Test
    fun jsonPostControllerViaClient() {
        val response = jsonHttpClient.post(JsonPostController.JsonRequest("1"))
        assertEquals("Ivan", response.name)
        assertEquals(100, response.value)
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
