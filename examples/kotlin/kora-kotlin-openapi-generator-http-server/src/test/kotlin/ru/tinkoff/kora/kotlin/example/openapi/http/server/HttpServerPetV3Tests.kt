package ru.tinkoff.kora.kotlin.example.openapi.http.server

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.kotlin.example.openapi.petV3.model.Pet
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Testcontainers
class HttpServerPetV3Tests {

    @Test
    fun postAndGetControllerSuccess() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val tags = JSONArray()
        tags.put(JSONObject().put("id", 1L).put("name", "tag"))
        val requestBody = JSONObject()
            .put("id", 1L)
            .put("name", "name")
            .put("status", Pet.StatusEnum.AVAILABLE.getValue())
            .put("category", JSONObject().put("id", 1L).put("name", "category"))
            .put("tags", tags)

        // when
        val requestBodyAsString = requestBody.toString()
        val requestPost = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyAsString))
            .uri(container.getURI().resolve("/v3/pet"))
            .timeout(Duration.ofSeconds(5))
            .build()
        val responsePost = httpClient.send(requestPost, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, responsePost.statusCode(), responsePost.body())

        // then
        val requestGet = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/v3/pet/${requestBody.get("id")}"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response = httpClient.send(requestGet, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), response.body())
        JSONAssert.assertEquals(requestBodyAsString, response.body(), JSONCompareMode.LENIENT)
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}
