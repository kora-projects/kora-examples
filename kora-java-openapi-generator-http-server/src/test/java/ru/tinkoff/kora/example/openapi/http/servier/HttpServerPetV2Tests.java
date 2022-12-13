package ru.tinkoff.kora.example.openapi.http.servier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.example.openapi.petV2.model.Pet;

@Testcontainers
class HttpServerPetV2Tests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void postAndGetControllerSuccess() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var tags = new JSONArray();
        tags.put(new JSONObject()
                .put("id", 1L)
                .put("name", "tag"));
        var requestBody = new JSONObject()
                .put("id", 1L)
                .put("name", "name")
                .put("status", Pet.StatusEnum.AVAILABLE.getValue())
                .put("category", new JSONObject()
                        .put("id", 1L)
                        .put("name", "category"))
                .put("tags", tags);

        // when
        var requestBodyAsString = requestBody.toString();
        var requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyAsString))
                .uri(container.getURI().resolve("/v2/pet"))
                .timeout(Duration.ofSeconds(5))
                .build();
        var responsePost = httpClient.send(requestPost, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePost.statusCode(), responsePost.body());

        // then
        var requestGet = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v2/pet/%s".formatted(requestBody.get("id"))))
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        JSONAssert.assertEquals(requestBodyAsString, response.body(), JSONCompareMode.LENIENT);
    }
}
