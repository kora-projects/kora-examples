package io.koraframework.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.koraframework.example.http.server.JsonPostController;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@KoraAppTest(TestApplication.class)
@Testcontainers
class JsonPostControllerIntegrationTests implements KoraAppTestConfigModifier {

    @Container
    private static final AppContainer container = AppContainer.build();

    @TestComponent
    private TestApplication.JsonHttpClient jsonHttpClient;

    @Override
    public @NotNull KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                testHttpClient {
                  url = ${HTTP_CLIENT_URL}
                  requestTimeout = 10s
                  telemetry.logging.enabled = true
                }
                """)
                .withSystemProperty("HTTP_CLIENT_URL", container.getURI().toString());
    }

    @Test
    void jsonPostControllerViaClient() {
        var response = jsonHttpClient.post(new JsonPostController.JsonRequest("1"));
        assertEquals("Ivan", response.name());
        assertEquals(100, response.value());
    }
}
