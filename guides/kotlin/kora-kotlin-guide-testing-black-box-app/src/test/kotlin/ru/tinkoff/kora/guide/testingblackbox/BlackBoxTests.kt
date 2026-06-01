package ru.tinkoff.kora.guide.testingblackbox

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

@Testcontainers
class BlackBoxTests {

    @Test
    fun createUserShouldCreateAndReturnUser() {
        val response =
            sendJson("POST", "/users", JSONObject().put("name", "John Doe").put("email", uniqueEmail("john")))

        assertEquals(201, response.statusCode())
        val responseBody = JSONObject(response.body())
        assertTrue(responseBody.has("id"))
        assertEquals("John Doe", responseBody.getString("name"))
    }

    @Test
    fun getUserShouldReturnUser() {
        val createResponse =
            sendJson("POST", "/users", JSONObject().put("name", "Jane Doe").put("email", uniqueEmail("jane")))
        val userId = JSONObject(createResponse.body()).getString("id")

        val getRequest = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getURI().resolve("/users/$userId"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val getResponse = HttpClient.newHttpClient().send(getRequest, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, getResponse.statusCode())
        val body = JSONObject(getResponse.body())
        assertEquals(userId, body.getString("id"))
        assertEquals("Jane Doe", body.getString("name"))
    }

    @Test
    fun getUserNotFoundShouldReturn404() {
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getURI().resolve("/users/999999"))
            .timeout(Duration.ofSeconds(10))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(404, response.statusCode())
    }

    @Test
    fun getUsersWithPaginationShouldReturnSizedResult() {
        sendJson("POST", "/users", JSONObject().put("name", "Alice").put("email", uniqueEmail("alice")))
        sendJson("POST", "/users", JSONObject().put("name", "Bob").put("email", uniqueEmail("bob")))
        sendJson("POST", "/users", JSONObject().put("name", "Charlie").put("email", uniqueEmail("charlie")))

        val request = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getURI().resolve("/users?page=0&size=2&sort=name"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertEquals(2, JSONArray(response.body()).length())
    }

    @Test
    fun updateUserShouldUpdateAndReturnUser() {
        val createResponse =
            sendJson("POST", "/users", JSONObject().put("name", "John").put("email", uniqueEmail("upd")))
        val userId = JSONObject(createResponse.body()).getString("id")

        val updateRequest = HttpRequest.newBuilder()
            .PUT(
                HttpRequest.BodyPublishers.ofString(
                    JSONObject().put("name", "John Updated").put("email", uniqueEmail("updated")).toString()
                )
            )
            .uri(APP.getURI().resolve("/users/$userId"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(10))
            .build()
        val updateResponse = HttpClient.newHttpClient().send(updateRequest, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, updateResponse.statusCode())
        assertEquals("John Updated", JSONObject(updateResponse.body()).getString("name"))
    }

    @Test
    fun deleteUserShouldRemoveUser() {
        val createResponse =
            sendJson("POST", "/users", JSONObject().put("name", "John").put("email", uniqueEmail("del")))
        val userId = JSONObject(createResponse.body()).getString("id")

        val deleteRequest = HttpRequest.newBuilder()
            .DELETE()
            .uri(APP.getURI().resolve("/users/$userId"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val deleteResponse = HttpClient.newHttpClient().send(deleteRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(204, deleteResponse.statusCode())

        val getRequest = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getURI().resolve("/users/$userId"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val getResponse = HttpClient.newHttpClient().send(getRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(404, getResponse.statusCode())
    }

    private fun sendJson(method: String, path: String, payload: JSONObject): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(APP.getURI().resolve(path))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(10))

        when (method) {
            "POST" -> request.POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            "PUT" -> request.PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
            else -> throw IllegalArgumentException("Unsupported method: $method")
        }

        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun uniqueEmail(prefix: String): String = "$prefix-${UUID.randomUUID()}@example.com"

    companion object {

        @Container
        @JvmStatic
        private val POSTGRES = PostgreSQLContainer("postgres:16-alpine")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("postgres")
            .withStartupTimeout(Duration.ofSeconds(30))
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer::class.java)))

        @Container
        @JvmStatic
        private val APP = AppContainer()
            .withNetwork(Network.SHARED)
            .dependsOn(POSTGRES)
            .withEnv("POSTGRES_JDBC_URL", "jdbc:postgresql://postgres:5432/${POSTGRES.databaseName}")
            .withEnv("POSTGRES_USER", POSTGRES.username)
            .withEnv("POSTGRES_PASS", POSTGRES.password)
    }
}
