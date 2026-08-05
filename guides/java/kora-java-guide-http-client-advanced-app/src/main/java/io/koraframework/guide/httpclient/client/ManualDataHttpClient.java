package io.koraframework.guide.httpclient.client;

import java.nio.charset.StandardCharsets;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.client.common.HttpClient;
import io.koraframework.http.client.common.request.HttpClientRequest;

@Component
public final class ManualDataHttpClient {

    private final HttpClient httpClient;
    private final $DataApiClient_Config dataApiConfig;
    private final ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    public ManualDataHttpClient(HttpClient httpClient, $DataApiClient_Config dataApiConfig, ApiKeyAuthInterceptor apiKeyAuthInterceptor) {
        this.httpClient = httpClient;
        this.dataApiConfig = dataApiConfig;
        this.apiKeyAuthInterceptor = apiKeyAuthInterceptor;
    }

    public String pingManualHandler() {
        var request = HttpClientRequest.of("GET", this.dataApiConfig.url() + "/manual/data/ping")
                .build();
        var response = this.httpClient.with(this.apiKeyAuthInterceptor)
                .execute(request)
                .toCompletableFuture()
                .join();
        if (response.code() != 200) {
            throw new IllegalStateException("Manual HTTP call failed with status " + response.code());
        }
        try (var body = response.body().asInputStream()) {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read manual HTTP response body", exception);
        }
    }
}
