package ru.tinkoff.kora.example.http.client;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;

@HttpClient(configPath = "httpClient.default")
public interface VoidHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    void sync();

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    Mono<Void> reactor();
}
