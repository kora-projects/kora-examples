package ru.tinkoff.kora.example.http.client;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.Header;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.annotation.Query;

@Component
@HttpClient(configPath = "httpClient.default")
public interface ReactorHttpClient {

    @HttpRoute(method = HttpMethod.GET, path = "/reactor/{path}")
    Mono<byte[]> get(@Path String path,
                     @Nullable @Query String query,
                     @Nullable @Header String header);
}
