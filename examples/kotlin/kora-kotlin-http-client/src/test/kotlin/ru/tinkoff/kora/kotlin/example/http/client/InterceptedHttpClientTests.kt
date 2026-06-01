package ru.tinkoff.kora.kotlin.example.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class InterceptedHttpClientTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: InterceptedHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun interceptedHttpClient() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/intercepted")
        ).respond(response().withBody("OK"))

        val response = httpClient.get()
        assertEquals(200, response.code())
    }
}
