package ru.tinkoff.kora.guide.observability

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

@Testcontainers
class ObservabilityHttpSmokeTest {
    @Test
    fun metricsEndpointExposesObservabilityDataOnPrivatePort() {
        val createRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody("Metrics User", uniqueEmail("metrics"))))
            .uri(APP.getURI().resolve("/users"))
            .header("Content-Type", "application/json")
            .header("X-Request-ID", "req-metrics")
            .timeout(Duration.ofSeconds(10))
            .build()
        val createResponse = HttpClient.newHttpClient().send(createRequest, HttpResponse.BodyHandlers.ofString())
        assertEquals(201, createResponse.statusCode())

        val metricsRequest = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getSystemURI().resolve("/metrics"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val metricsResponse = HttpClient.newHttpClient().send(metricsRequest, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, metricsResponse.statusCode())
        assertTrue(
            metricsResponse.body().contains("http_server_active_requests") || metricsResponse.body()
                .contains("http_server_request_duration")
        )
        assertTrue(
            metricsResponse.body().contains("logback_events_total") || metricsResponse.body()
                .contains("jvm_memory_used_bytes") || metricsResponse.body().contains("process_cpu_usage")
        )
    }

    @Test
    fun managementHealthEndpointsAreAvailableOnPrivatePort() {
        val livenessRequest = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getSystemURI().resolve("/system/liveness"))
            .timeout(Duration.ofSeconds(10))
            .build()
        val readinessRequest = HttpRequest.newBuilder()
            .GET()
            .uri(APP.getSystemURI().resolve("/system/readiness"))
            .timeout(Duration.ofSeconds(10))
            .build()

        val livenessResponse = HttpClient.newHttpClient().send(livenessRequest, HttpResponse.BodyHandlers.ofString())
        val readinessResponse = HttpClient.newHttpClient().send(readinessRequest, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, livenessResponse.statusCode())
        assertEquals(200, readinessResponse.statusCode())
    }

    @Test
    fun requestLoggingIncludesTracingContext() {
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody("Trace User", uniqueEmail("trace"))))
            .uri(APP.getURI().resolve("/users"))
            .header("Content-Type", "application/json")
            .header("X-Request-ID", "req-trace")
            .timeout(Duration.ofSeconds(10))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(201, response.statusCode())

        Awaitility.await().atMost(Duration.ofSeconds(5)).until {
            val logs = APP.getLogs(OutputFrame.OutputType.STDOUT)
            logs.contains("traceId=") && logs.contains("spanId=")
        }
    }

    private fun jsonBody(name: String, email: String): String = "{\"name\":\"$name\",\"email\":\"$email\"}"

    private fun uniqueEmail(prefix: String): String = "$prefix-${UUID.randomUUID()}@example.com"

    companion object {
        @Container
        @JvmStatic
        val APP = AppContainer()
    }
}
