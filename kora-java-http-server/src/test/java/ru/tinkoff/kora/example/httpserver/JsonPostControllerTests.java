package ru.tinkoff.kora.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class JsonPostControllerTests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void jsonPostController() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();

        // then
        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"Ivan\"}"))
                .uri(container.getURI().resolve("/json"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        JSONAssert.assertEquals(
                """
                        {"name":"Ivan","value":100}""", response.body(), JSONCompareMode.STRICT);
    }
}
