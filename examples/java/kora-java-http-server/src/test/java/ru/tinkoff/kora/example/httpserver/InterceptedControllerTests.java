package ru.tinkoff.kora.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Testcontainers
class InterceptedControllerTests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void interceptedController() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();

        // when
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/intercepted"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Hello world", response.body(), response.body());

        // then
        var interceptedLogs = Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> container.getLogs().split("\n"),
                        logs -> Arrays.stream(logs).anyMatch(log -> log.endsWith("Method Level Interceptor")));
        assertTrue(Arrays.stream(interceptedLogs).anyMatch(log -> log.endsWith("Server Level Interceptor")));
        assertTrue(Arrays.stream(interceptedLogs).anyMatch(log -> log.endsWith("Controller Level Interceptor")));
        assertTrue(Arrays.stream(interceptedLogs).anyMatch(log -> log.endsWith("Method Level Interceptor")));
    }
}
