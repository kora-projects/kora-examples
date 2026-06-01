package ru.tinkoff.kora.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.example.http.server.JsonPostController;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

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
