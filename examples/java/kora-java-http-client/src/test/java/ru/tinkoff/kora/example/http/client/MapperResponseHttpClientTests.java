package ru.tinkoff.kora.example.http.client;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class MapperResponseHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private MapperResponseHttpClient httpClient;

    @Test
    void mapperResponseHttpClientSuccess() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/mapping_by_code/1"))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withStatusCode(200)
                                .withBody("OK"));

        // then
        var response = httpClient.get("1");
        assertNull(response.error());
        assertNotNull(response.payload());
        assertEquals("OK", response.payload().message());
    }

    @Test
    void requestMappingSuccess() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/mapping_by_code/2"))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withStatusCode(400)
                                .withBody("Ops"));

        // then
        var response = httpClient.get("2");
        assertNull(response.payload());
        assertNotNull(response.error());
        assertEquals(400, response.error().code());
        assertEquals("Ops", response.error().message());
    }
}
