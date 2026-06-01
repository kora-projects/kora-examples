package ru.tinkoff.kora.guide.testingblackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class BlackBoxTests {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("postgres")
            .withStartupTimeout(Duration.ofSeconds(30))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer.class)));

    @Container
    private static final AppContainer APP = new AppContainer()
            .withNetwork(Network.SHARED)
            .dependsOn(POSTGRES)
            .withEnv("POSTGRES_JDBC_URL", "jdbc:postgresql://postgres:5432/" + POSTGRES.getDatabaseName())
            .withEnv("POSTGRES_USER", POSTGRES.getUsername())
            .withEnv("POSTGRES_PASS", POSTGRES.getPassword());

    @Test
    void createUser_ShouldCreateAndReturnUser() throws Exception {
        var response = sendJson("POST", "/users", new JSONObject()
                .put("name", "John Doe")
                .put("email", uniqueEmail("john")));

        assertEquals(201, response.statusCode());
        var responseBody = new JSONObject(response.body());
        assertTrue(responseBody.has("id"));
        assertEquals("John Doe", responseBody.getString("name"));
    }

    @Test
    void getUser_ShouldReturnUser() throws Exception {
        var createResponse = sendJson("POST", "/users", new JSONObject()
                .put("name", "Jane Doe")
                .put("email", uniqueEmail("jane")));
        var userId = new JSONObject(createResponse.body()).getString("id");

        var getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getURI().resolve("/users/" + userId))
                .timeout(Duration.ofSeconds(10))
                .build();
        var getResponse = HttpClient.newHttpClient().send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        var body = new JSONObject(getResponse.body());
        assertEquals(userId, body.getString("id"));
        assertEquals("Jane Doe", body.getString("name"));
    }

    @Test
    void getUser_NotFound_ShouldReturn404() throws Exception {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getURI().resolve("/users/999999"))
                .timeout(Duration.ofSeconds(10))
                .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void getUsers_WithPagination_ShouldReturnSizedResult() throws Exception {
        sendJson("POST", "/users", new JSONObject().put("name", "Alice").put("email", uniqueEmail("alice")));
        sendJson("POST", "/users", new JSONObject().put("name", "Bob").put("email", uniqueEmail("bob")));
        sendJson("POST", "/users", new JSONObject().put("name", "Charlie").put("email", uniqueEmail("charlie")));

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getURI().resolve("/users?page=0&size=2&sort=name"))
                .timeout(Duration.ofSeconds(10))
                .build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        var users = new JSONArray(response.body());
        assertEquals(2, users.length());
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        var createResponse = sendJson("POST", "/users", new JSONObject()
                .put("name", "John")
                .put("email", uniqueEmail("upd")));
        var userId = new JSONObject(createResponse.body()).getString("id");

        var updateRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(new JSONObject()
                        .put("name", "John Updated")
                        .put("email", uniqueEmail("updated"))
                        .toString()))
                .uri(APP.getURI().resolve("/users/" + userId))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        var updateResponse = HttpClient.newHttpClient().send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode());
        var body = new JSONObject(updateResponse.body());
        assertEquals("John Updated", body.getString("name"));
    }

    @Test
    void deleteUser_ShouldRemoveUser() throws Exception {
        var createResponse = sendJson("POST", "/users", new JSONObject()
                .put("name", "John")
                .put("email", uniqueEmail("del")));
        var userId = new JSONObject(createResponse.body()).getString("id");

        var deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(APP.getURI().resolve("/users/" + userId))
                .timeout(Duration.ofSeconds(10))
                .build();
        var deleteResponse = HttpClient.newHttpClient().send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, deleteResponse.statusCode());

        var getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getURI().resolve("/users/" + userId))
                .timeout(Duration.ofSeconds(10))
                .build();
        var getResponse = HttpClient.newHttpClient().send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    private HttpResponse<String> sendJson(String method, String path, JSONObject payload) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(APP.getURI().resolve(path))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10));

        if ("POST".equals(method)) {
            request.POST(HttpRequest.BodyPublishers.ofString(payload.toString()));
        } else if ("PUT".equals(method)) {
            request.PUT(HttpRequest.BodyPublishers.ofString(payload.toString()));
        } else {
            throw new IllegalArgumentException("Unsupported method: " + method);
        }

        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
