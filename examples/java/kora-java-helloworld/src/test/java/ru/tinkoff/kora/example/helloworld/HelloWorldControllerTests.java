package ru.tinkoff.kora.example.helloworld;

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
class HelloWorldControllerTests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void helloWorld() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/hello/world"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // when
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // then
        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Hello World", response.body());
    }

    @Test
    void helloWorldJson() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/hello/world/json"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // when
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());

        // then
        JSONAssert.assertEquals("{\"greeting\":\"Hello World\"}", response.body(), JSONCompareMode.STRICT);
    }
}
