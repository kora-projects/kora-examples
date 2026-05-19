package ru.tinkoff.kora.example.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class ReactorHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private ReactorHttpClient httpClient;

    @Test
    void reactorHttpClient() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/reactor/path")
                        .withQueryStringParameter("query", "query")
                        .withHeader("header", "header"))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("OK"));

        // then
        var response = httpClient.get("path", "query", "header").block();
        assertNotNull(response);
        assertEquals("OK", new String(response, StandardCharsets.UTF_8));
    }
}
