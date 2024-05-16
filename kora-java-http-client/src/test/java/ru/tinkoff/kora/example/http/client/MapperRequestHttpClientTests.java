package ru.tinkoff.kora.example.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
class MapperRequestHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private MapperRequestHttpClient httpClient;

    @Test
    void mapperRequestHttpClient() {
        // given
        mockserverConnection.client().when(org.mockserver.model.HttpRequest.request()
                .withMethod("POST")
                .withPath("/mapping_request")
                .withBody("123"))
                .respond(org.mockserver.model.HttpResponse.response()
                        .withStatusCode(200)
                        .withBody("OK"));

        // then
        var response = httpClient.post(new MapperRequestHttpClient.UserBody("123"));
        assertNotNull(response);
        assertEquals(200, response.code());
    }
}
