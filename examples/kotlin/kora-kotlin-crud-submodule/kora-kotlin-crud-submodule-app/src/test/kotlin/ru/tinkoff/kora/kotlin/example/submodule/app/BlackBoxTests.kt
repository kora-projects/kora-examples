package ru.tinkoff.kora.kotlin.example.submodule.app

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.Network
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@TestcontainersPostgreSQL(
    network = Network(shared = true),
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.FLYWAY,
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD
    )
)
class BlackBoxTests {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @Test
    fun addPet() {
        val requestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        val response = send(
            HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )

        assertEquals(200, response.statusCode(), response.body())
        connection.assertCountsEquals(1, "pets")
        connection.assertCountsEquals(1, "categories")
        val responseBody = JSONObject(response.body())
        assertNotNull(responseBody.query("/id"))
        assertNotEquals(0L, responseBody.query("/id"))
        assertNotNull(responseBody.query("/status"))
        assertEquals(requestBody.query("/name"), responseBody.query("/name"))
        assertNotNull(responseBody.query("/category/id"))
        assertEquals(requestBody.query("/category/name"), responseBody.query("/category/name"))
    }

    @Test
    fun getPetNotFound() {
        val response = send(
            HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/1"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )

        assertEquals(404, response.statusCode(), response.body())
    }

    @Test
    fun getPet() {
        val createResponseBody = createPet()

        val getResponse = send(
            HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/${createResponseBody.query("/id")}"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )

        assertEquals(200, getResponse.statusCode(), getResponse.body())
        JSONAssert.assertEquals(
            createResponseBody.toString(),
            JSONObject(getResponse.body()).toString(),
            JSONCompareMode.LENIENT
        )
    }

    @Test
    fun updatePet() {
        val createResponseBody = createPet()
        val updateRequestBody = JSONObject()
            .put("name", "doggie2")
            .put("status", "pending")
            .put("category", JSONObject().put("name", "Dogs2"))

        val updateResponse = send(
            HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(updateRequestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets/${createResponseBody.query("/id")}"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )
        assertEquals(200, updateResponse.statusCode(), updateResponse.body())

        val getResponse = send(
            HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/v3/pets/${createResponseBody.query("/id")}"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )

        assertEquals(200, getResponse.statusCode(), getResponse.body())
        JSONAssert.assertEquals(
            JSONObject(updateResponse.body()).toString(),
            JSONObject(getResponse.body()).toString(),
            JSONCompareMode.LENIENT
        )
    }

    @Test
    fun deletePet() {
        val createResponseBody = createPet()

        val deleteResponse = send(
            HttpRequest.newBuilder()
                .DELETE()
                .uri(container.getURI().resolve("/v3/pets/${createResponseBody.query("/id")}"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )

        assertEquals(200, deleteResponse.statusCode(), deleteResponse.body())
        connection.assertCountsEquals(0, "pets")
    }

    private fun createPet(): JSONObject {
        val requestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        val createResponse = send(
            HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .uri(container.getURI().resolve("/v3/pets"))
                .timeout(Duration.ofSeconds(5))
                .build()
        )
        assertEquals(200, createResponse.statusCode(), createResponse.body())
        connection.assertCountsEquals(1, "pets")
        connection.assertCountsEquals(1, "categories")
        return JSONObject(createResponse.body())
    }

    private fun send(request: HttpRequest): HttpResponse<String> {
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        private val container = AppContainer.build()
            .withNetwork(org.testcontainers.containers.Network.SHARED)

        @BeforeAll
        @JvmStatic
        fun setup(@ConnectionPostgreSQL connection: JdbcConnection) {
            val params = connection.paramsInNetwork().orElseThrow()
            container.withEnv(
                mapOf(
                    "POSTGRES_JDBC_URL" to params.jdbcUrl(),
                    "POSTGRES_USER" to params.username(),
                    "POSTGRES_PASS" to params.password(),
                    "CACHE_MAX_SIZE" to "0",
                    "RETRY_ATTEMPTS" to "0",
                    "LOGGING_LEVEL_KORA" to "INFO",
                    "LOGGING_LEVEL_APP" to "DEBUG",
                    "LOGGING_LEVEL_KORA_HTTP_SERVER" to "TRACE",
                    "LOGGING_LEVEL_KORA_DATABASE" to "TRACE",
                    "LOGGING_LEVEL_KORA_RESILIENT" to "TRACE",
                    "LOGGING_LEVEL_KORA_CACHE" to "TRACE"
                )
            )
            container.start()
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            container.stop()
        }
    }
}
