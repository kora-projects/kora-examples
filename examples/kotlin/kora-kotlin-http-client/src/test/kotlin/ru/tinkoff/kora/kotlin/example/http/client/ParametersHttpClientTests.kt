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
class ParametersHttpClientTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: ParametersHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun parametersHttpClient() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withPath("/parameters/path")
                .withQueryStringParameter("query", "query")
                .withQueryStringParameter("queries", "query1", "query2")
                .withHeader("header", "header")
                .withHeader("headers", "header1", "header2")
        ).respond(response().withBody("OK"))

        val response = httpClient.post(
            "path",
            "query",
            listOf("query1", "query2"),
            "header",
            listOf("header1", "header2")
        )
        assertEquals(200, response.code())
        assertEquals("OK", response.body())
    }
}
