package ru.tinkoff.kora.kotlin.example.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class SuspendHttpClientTests : KoraAppTestConfigModifier {
    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: SuspendHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun suspendHttpClient() = runBlocking {
        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/suspend/path")
                .withQueryStringParameter("query", "query")
                .withHeader("header", "header")
        ).respond(response().withBody("Hello world"))

        assertArrayEquals("Hello world".toByteArray(), httpClient.get("path", "query", "header").body())
    }
}
