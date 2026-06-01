package ru.tinkoff.kora.kotlin.example.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class MapperRequestHttpClientTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: MapperRequestHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun mapperRequestHttpClient() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withPath("/mapping_request")
                .withBody("123")
        ).respond(response().withStatusCode(200).withBody("OK"))

        val response = httpClient.post(MapperRequestHttpClient.UserBody("123"))
        assertNotNull(response)
        assertEquals(200, response.code())
    }
}
