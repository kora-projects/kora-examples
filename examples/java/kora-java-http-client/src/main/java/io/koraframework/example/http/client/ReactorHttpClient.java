package io.koraframework.example.http.client;

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;

@HttpClient("httpClient.default")
public interface ReactorHttpClient {

    @HttpRoute(method = HttpMethod.GET, path = "/reactor/{path}")
    Mono<byte[]> get(@Path String path,
                     @Nullable @Query String query,
                     @Nullable @Header String header);
}
