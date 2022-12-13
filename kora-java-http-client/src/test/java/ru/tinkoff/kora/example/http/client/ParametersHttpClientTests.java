package ru.tinkoff.kora.example.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ContainerMockserverConnection;
import io.goodforgod.testcontainers.extensions.mockserver.MockserverConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockserver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockserver(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class ParametersHttpClientTests implements KoraAppTestConfigModifier {

    @ContainerMockserverConnection
    private MockserverConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private ParametersHttpClient httpClient;

    @Test
    void parametersHttpClient() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/parameters/path")
                        .withQueryStringParameter("query", "query")
                        .withQueryStringParameter("queries", "query1", "query2")
                        .withHeader("header", "header")
                        .withHeader("headers", "header1", "header2"))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("OK"));

        // then
        var response = httpClient.post("path",
                "query", List.of("query1", "query2"),
                "header", List.of("header1", "header2"));
        assertEquals(200, response.code());
        assertEquals("OK", response.body());
    }
}
