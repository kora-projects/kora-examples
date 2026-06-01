package ru.tinkoff.kora.example.graalvm.crud.cassandra;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.Network;
import io.goodforgod.testcontainers.extensions.redis.ConnectionRedis;
import io.goodforgod.testcontainers.extensions.redis.RedisConnection;
import io.goodforgod.testcontainers.extensions.redis.TestcontainersRedis;
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla;
import io.goodforgod.testcontainers.extensions.scylla.Migration;
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection;
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@TestcontainersScylla(
        network = @Network(shared = true),
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                locations = "migrations",
                engine = Migration.Engines.SCRIPTS,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@TestcontainersRedis(
        network = @Network(shared = true),
        mode = ContainerMode.PER_RUN)
class BlackBoxTests {

    private static final AppContainer container = AppContainer.build()
            .withNetwork(org.testcontainers.containers.Network.SHARED);

    @ConnectionScylla
    private ScyllaConnection connection;

    @BeforeEach
    public void setup(@ConnectionScylla ScyllaConnection cassandraConnection,
                      @ConnectionRedis RedisConnection redisConnection) {
        if (!container.isRunning()) {
            var paramsCassandra = cassandraConnection.paramsInNetwork().orElseThrow();
            var paramsRedis = redisConnection.paramsInNetwork().orElseThrow();
            container.withEnv(Map.ofEntries(
                    Map.entry("CASSANDRA_CONTACT_POINTS", paramsCassandra.contactPoint()),
                    Map.entry("CASSANDRA_USER", paramsCassandra.username()),
                    Map.entry("CASSANDRA_PASS", paramsCassandra.password()),
                    Map.entry("CASSANDRA_DC", paramsCassandra.datacenter()),
                    Map.entry("CASSANDRA_KEYSPACE", paramsCassandra.keyspace()),
                    Map.entry("REDIS_URL", paramsRedis.uri().toString()),
                    Map.entry("REDIS_USER", paramsRedis.username()),
                    Map.entry("REDIS_PASS", paramsRedis.password()),
                    Map.entry("CACHE_MAX_SIZE", "0"),
                    Map.entry("RETRY_ATTEMPTS", "0"),
                    Map.entry("LOGGING_LEVEL_KORA", "INFO"),
                    Map.entry("LOGGING_LEVEL_APP", "DEBUG"),
                    Map.entry("LOGGING_LEVEL_KORA_HTTP_SERVER", "TRACE"),
                    Map.entry("LOGGING_LEVEL_KORA_DATABASE", "TRACE"),
                    Map.entry("LOGGING_LEVEL_KORA_RESILIENT", "TRACE"),
                    Map.entry("LOGGING_LEVEL_KORA_CACHE", "TRACE")));

            container.start();
        }
    }

    @AfterAll
    public static void cleanup() {
        container.stop();
    }

    @Test
    void addPet() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var requestBody = new JSONObject()
                .put("name", "doggie")
                .put("category", new JSONObject()
                        .put("name", "Dogs"));

        // when
        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());

        // then
        connection.assertCountsEquals(1, "pets");
        var responseBody = new JSONObject(response.body());
        assertNotNull(responseBody.query("/id"));
        assertNotEquals(0L, responseBody.query("/id"));
        assertNotNull(responseBody.query("/status"));
        assertEquals(requestBody.query("/name"), responseBody.query("/name"));
        assertNotNull(responseBody.query("/category/id"));
        assertEquals(requestBody.query("/category/name"), responseBody.query("/category/name"));
    }

    @Test
    void getPet() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var createRequestBody = new JSONObject()
                .put("name", "doggie")
                .put("category", new JSONObject()
                        .put("name", "Dogs"));

        // when
        var createRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode(), createResponse.body());
        connection.assertCountsEquals(1, "pets");
        var createResponseBody = new JSONObject(createResponse.body());

        // then
        var getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/" + createResponseBody.query("/id")))
                .timeout(Duration.ofSeconds(5))
                .build();

        var getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), getResponse.body());

        var getResponseBody = new JSONObject(getResponse.body());
        JSONAssert.assertEquals(createResponseBody.toString(), getResponseBody.toString(), JSONCompareMode.LENIENT);
    }

    @Test
    void getPetNotFound() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();

        // when
        var getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/1"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // then
        var getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode(), getResponse.body());
    }

    @Test
    void updatePet() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var createRequestBody = new JSONObject()
                .put("name", "doggie")
                .put("category", new JSONObject()
                        .put("name", "Dogs"));

        var createRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode(), createResponse.body());
        connection.assertCountsEquals(1, "pets");
        var createResponseBody = new JSONObject(createResponse.body());

        // when
        var updateRequestBody = new JSONObject()
                .put("name", "doggie2")
                .put("status", "pending")
                .put("category", new JSONObject()
                        .put("name", "Dogs2"));

        var updateRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(updateRequestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets/" + createResponseBody.query("/id")))
                .timeout(Duration.ofSeconds(5))
                .build();

        var updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, updateResponse.statusCode(), updateResponse.body());
        var updateResponseBody = new JSONObject(updateResponse.body());

        // then
        var getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/" + createResponseBody.query("/id")))
                .timeout(Duration.ofSeconds(5))
                .build();

        var getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode(), getResponse.body());

        var getResponseBody = new JSONObject(getResponse.body());
        JSONAssert.assertEquals(updateResponseBody.toString(), getResponseBody.toString(), JSONCompareMode.LENIENT);
    }

    @Test
    void deletePet() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var createRequestBody = new JSONObject()
                .put("name", "doggie")
                .put("category", new JSONObject()
                        .put("name", "Dogs"));

        var createRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build();

        var createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode(), createResponse.body());
        connection.assertCountsEquals(1, "pets");
        var createResponseBody = new JSONObject(createResponse.body());

        // when
        var deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(container.getURI().resolve("/v3/pets/" + createResponseBody.query("/id")))
                .timeout(Duration.ofSeconds(5))
                .build();

        var deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), deleteResponse.body());

        // then
        connection.assertCountsEquals(0, "pets");
    }
}
