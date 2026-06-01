package ru.tinkoff.kora.kotlin.example.telemetry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Testcontainers
class TelemetryTests {

    companion object {
        @Container
        private val telemetryContainer = TelemetryContainer()
            .withNetworkAliases("otel")
            .withNetwork(Network.SHARED)

        @Container
        private val container = AppContainer.build()
            .withNetworkAliases("app")
            .withNetwork(Network.SHARED)
            .withEnv("METRIC_COLLECTOR_ENDPOINT", telemetryContainer.getCollectorURIInNetwork().toString())
    }

    @Test
    fun tracingTelemetryExported() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val requestTest = HttpRequest.newBuilder()
            .GET()
            .uri(container.getURI().resolve("/text"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // when
        val responseTest = httpClient.send(requestTest, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, responseTest.statusCode())

        // then
        Awaitility.await().atMost(Duration.ofSeconds(5))
            .until {
                val logs = telemetryContainer.getLogs(OutputFrame.OutputType.STDERR)
                val logsSplit = logs.split("\n")
                logsSplit.any {
                    it.replace('\t', ' ').endsWith(
                        "TracesExporter {\"kind\": \"exporter\", \"data_type\": \"traces\", \"name\": \"logging\", \"#spans\": 1}"
                    )
                }
            }
    }

    @Test
    fun healthLivenessFails() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getSystemURI().resolve("/liveness"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // then
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(503, response.statusCode())
        assertEquals("Error", response.body())
    }

    @Test
    fun healthReadinessFails() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getSystemURI().resolve("/readiness"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // then
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(503, response.statusCode())
        assertEquals("Error", response.body())
    }

    @Test
    fun metricsSuccess() {
        // given
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(container.getSystemURI().resolve("/metrics"))
            .timeout(Duration.ofSeconds(5))
            .build()

        // then
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode())
        assertFalse(response.body().isBlank())
    }
}
