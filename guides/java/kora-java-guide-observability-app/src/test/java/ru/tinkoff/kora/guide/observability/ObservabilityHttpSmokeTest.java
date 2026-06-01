package ru.tinkoff.kora.guide.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Testcontainers
class ObservabilityHttpSmokeTest {

    @Container
    private static final AppContainer APP = new AppContainer();

    @Test
    void metricsEndpointExposesObservabilityDataOnPrivatePort() throws Exception {
        var createRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody("Metrics User", uniqueEmail("metrics"))))
                .uri(APP.getURI().resolve("/users"))
                .header("Content-Type", "application/json")
                .header("X-Request-ID", "req-metrics")
                .timeout(Duration.ofSeconds(10))
                .build();
        var createResponse = HttpClient.newHttpClient().send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        var metricsRequest = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getSystemURI().resolve("/metrics"))
                .timeout(Duration.ofSeconds(10))
                .build();
        var metricsResponse = HttpClient.newHttpClient().send(metricsRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, metricsResponse.statusCode());
        assertTrue(metricsResponse.body().contains("http_server_active_requests") || metricsResponse.body().contains("http_server_request_duration"));
        assertTrue(metricsResponse.body().contains("logback_events_total") || metricsResponse.body().contains("jvm_memory_used_bytes") || metricsResponse.body().contains("process_cpu_usage"));
    }

    @Test
    void managementHealthEndpointsAreAvailableOnPrivatePort() throws Exception {
        var livenessRequest = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getSystemURI().resolve("/system/liveness"))
                .timeout(Duration.ofSeconds(10))
                .build();
        var readinessRequest = HttpRequest.newBuilder()
                .GET()
                .uri(APP.getSystemURI().resolve("/system/readiness"))
                .timeout(Duration.ofSeconds(10))
                .build();

        var livenessResponse = HttpClient.newHttpClient().send(livenessRequest, HttpResponse.BodyHandlers.ofString());
        var readinessResponse = HttpClient.newHttpClient().send(readinessRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, livenessResponse.statusCode());
        assertEquals(200, readinessResponse.statusCode());
    }

    @Test
    void requestLoggingIncludesTracingContext() throws Exception {
        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody("Trace User", uniqueEmail("trace"))))
                .uri(APP.getURI().resolve("/users"))
                .header("Content-Type", "application/json")
                .header("X-Request-ID", "req-trace")
                .timeout(Duration.ofSeconds(10))
                .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> {
            var logs = APP.getLogs(OutputFrame.OutputType.STDOUT);
            return logs.contains("traceId=") && logs.contains("spanId=");
        });
    }

    private String jsonBody(String name, String email) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
