package ru.tinkoff.kora.example.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Testcontainers
class TelemetryTests {

    @Container
    private static final TelemetryContainer telemetryContainer = new TelemetryContainer()
            .withNetworkAliases("otel")
            .withNetwork(Network.SHARED);

    @Container
    private static final AppContainer container = AppContainer.build()
            .withNetworkAliases("app")
            .withNetwork(Network.SHARED)
            .withEnv("METRIC_COLLECTOR_ENDPOINT", telemetryContainer.getCollectorURIInNetwork().toString());

    @Test
    void tracingTelemetryExported() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var requestTest = HttpRequest.newBuilder()
                .GET()
                .uri(container.getURI().resolve("/text"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // when
        var responseTest = httpClient.send(requestTest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseTest.statusCode());

        // then
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    final String logs = telemetryContainer.getLogs(OutputFrame.OutputType.STDERR);
                    final String[] logsSplit = logs.split("\n");
                    final String lastLog = logsSplit[logsSplit.length - 1].replace('\t', ' ');
                    return lastLog.endsWith(
                            "TracesExporter {\"kind\": \"exporter\", \"data_type\": \"traces\", \"name\": \"logging\", \"#spans\": 1}");
                });
    }

    @Test
    void healthLivenessFails() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getSystemURI().resolve("/liveness"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // then
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(503, response.statusCode());
        assertEquals("Error", response.body());
    }

    @Test
    void healthReadinessFails() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getSystemURI().resolve("/readiness"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // then
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(503, response.statusCode());
        assertEquals("Error", response.body());
    }

    @Test
    void metricsSuccess() throws Exception {
        // given
        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(container.getSystemURI().resolve("/metrics"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // then
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertFalse(response.body().isBlank());
    }
}
