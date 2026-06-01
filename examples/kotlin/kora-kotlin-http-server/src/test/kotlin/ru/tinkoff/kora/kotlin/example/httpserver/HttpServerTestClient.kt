package ru.tinkoff.kora.kotlin.example.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal fun AppContainer.get(path: String): HttpResponse<String> {
    val request = HttpRequest.newBuilder()
        .GET()
        .uri(getURI().resolve(path))
        .timeout(Duration.ofSeconds(5))
        .build()
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
}

internal fun AppContainer.assertBody(path: String, code: Int, body: String) {
    val response = get(path)
    assertEquals(code, response.statusCode(), response.body())
    assertEquals(body, response.body())
}
