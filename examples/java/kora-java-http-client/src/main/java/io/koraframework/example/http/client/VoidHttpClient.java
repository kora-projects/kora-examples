package io.koraframework.example.http.client;

import reactor.core.publisher.Mono;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;

@HttpClient(configPath = "httpClient.default")
public interface VoidHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    void sync();

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    Mono<Void> reactor();
}
