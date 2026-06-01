package ru.tinkoff.kora.kotlin.example.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class MapperResponseHttpClientTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: MapperResponseHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun mapperResponseHttpClientSuccess() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/mapping_by_code/1")
        ).respond(response().withStatusCode(200).withBody("OK"))

        val response = httpClient.get("1")
        assertNull(response.error)
        assertNotNull(response.payload)
        assertEquals("OK", response.payload?.message)
    }

    @Test
    fun requestMappingSuccess() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("GET")
                .withPath("/mapping_by_code/2")
        ).respond(response().withStatusCode(400).withBody("Ops"))

        val response = httpClient.get("2")
        assertNull(response.payload)
        assertNotNull(response.error)
        assertEquals(400, response.error?.code)
        assertEquals("Ops", response.error?.message)
    }
}
