package ru.tinkoff.kora.example.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockserver.model.StringBody;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class JsonHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private JsonHttpClient httpClient;

    @Test
    void jsonHttpClient() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/json")
                        .withBody(new StringBody("{\"id\":\"1\"}")))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("{\"name\":\"Ivan\",\"value\":100}"));

        // then
        var response = httpClient.post(new JsonHttpClient.JsonRequest("1"));
        assertEquals("Ivan", response.name());
        assertEquals(100, response.value());
    }
}
