package io.koraframework.example.http.client;

import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;

@HttpClient("httpClient.default")
public interface VoidHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    void sync();
}
