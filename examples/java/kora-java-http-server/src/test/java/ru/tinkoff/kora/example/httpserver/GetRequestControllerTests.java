package ru.tinkoff.kora.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class GetRequestControllerTests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void getRequestController() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();

        // then
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/parameters/path?query=query&Queries=queries1,queries2"))
                .header("header", "header")
                .header("Headers", "1")
                .header("Headers", "2")
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Path: path, Query: query, Queries: [queries1,queries2], Header: header, Headers: [1, 2]",
                response.body(), response.body());
    }

    @Test
    void getRequestControllerOptionalMissingSuccess() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();

        // then
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/request"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Path: /request, Query: null, Queries: null, Header: null, Headers: null", response.body(), response.body());
    }
}
