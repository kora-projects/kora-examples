package ru.tinkoff.kora.kotlin.example.crud

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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testcontainers.containers.Network.*
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
class PetControllerTests(@ConnectionPostgreSQL val connection: JdbcConnection) {

    companion object {

        private val container: AppContainer = AppContainer.build().withNetwork(SHARED)

        @JvmStatic
        @BeforeAll
        fun setup(@ConnectionPostgreSQL connection: JdbcConnection) {
            val params = connection.paramsInNetwork().orElseThrow()
            container.withEnv(
                mapOf(
                    "POSTGRES_JDBC_URL" to params.jdbcUrl(),
                    "POSTGRES_USER" to params.username(),
                    "POSTGRES_PASS" to params.password(),
                    "CACHE_EXPIRE_WRITE" to "0s"
                )
            )
            container.start()
        }

        @JvmStatic
        @AfterAll
        fun cleanup() {
            container.stop()
        }
    }

    @Test
    fun addPet() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val requestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        // when
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .uri(container.uri.resolve("/v3/pets"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())

        // then
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
    fun getPet() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val createRequestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        // when
        val createRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
            .uri(container.uri.resolve("/v3/pets"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, createResponse.statusCode(), createResponse.body())
        connection.assertCountsEquals(1, "pets")
        connection.assertCountsEquals(1, "categories")
        val createResponseBody = JSONObject(createResponse.body())

        // then
        val getRequest = HttpRequest.newBuilder()
            .GET()
            .uri(container.uri.resolve("/v3/pets/" + createResponseBody.query("/id")))
            .timeout(Duration.ofSeconds(5))
            .build()

        val getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, getResponse.statusCode(), getResponse.body())

        val getResponseBody = JSONObject(getResponse.body())
        JSONAssert.assertEquals(createResponseBody.toString(), getResponseBody.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun updatePet() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val createRequestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        val createRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
            .uri(container.uri.resolve("/v3/pets"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, createResponse.statusCode(), createResponse.body())
        connection.assertCountsEquals(1, "pets")
        connection.assertCountsEquals(1, "categories")
        val createResponseBody = JSONObject(createResponse.body())

        // when
        val updateRequestBody = JSONObject()
            .put("name", "doggie2")
            .put("status", "pending")
            .put("category", JSONObject().put("name", "Dogs2"))

        val updateRequest = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(updateRequestBody.toString()))
            .uri(container.uri.resolve("/v3/pets/" + createResponseBody.query("/id")))
            .timeout(Duration.ofSeconds(5))
            .build()

        val updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, updateResponse.statusCode(), updateResponse.body())
        val updateResponseBody = JSONObject(updateResponse.body())

        // then
        val getRequest = HttpRequest.newBuilder()
            .GET()
            .uri(container.uri.resolve("/v3/pets/" + createResponseBody.query("/id")))
            .timeout(Duration.ofSeconds(5))
            .build()

        val getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, createResponse.statusCode(), getResponse.body())

        val getResponseBody = JSONObject(getResponse.body())
        JSONAssert.assertEquals(updateResponseBody.toString(), getResponseBody.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun deletePet() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val createRequestBody = JSONObject()
            .put("name", "doggie")
            .put("category", JSONObject().put("name", "Dogs"))

        val createRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(createRequestBody.toString()))
            .uri(container.uri.resolve("/v3/pets"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, createResponse.statusCode(), createResponse.body())
        connection.assertCountsEquals(1, "pets")
        connection.assertCountsEquals(1, "categories")
        val createResponseBody = JSONObject(createResponse.body())

        // when
        val deleteRequest = HttpRequest.newBuilder()
            .DELETE()
            .uri(container.uri.resolve("/v3/pets/" + createResponseBody.query("/id")))
            .timeout(Duration.ofSeconds(5))
            .build()

        val deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, deleteResponse.statusCode(), deleteResponse.body())

        // then
        connection.assertCountsEquals(0, "pets")
    }
}
