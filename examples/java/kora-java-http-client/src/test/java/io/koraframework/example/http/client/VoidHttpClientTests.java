package io.koraframework.example.http.client;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class VoidHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private VoidHttpClient httpClient;

    @Test
    void voidPostSyncRequestSuccess() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/void"))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("OK"));

        // then
        httpClient.sync();
    }
}
