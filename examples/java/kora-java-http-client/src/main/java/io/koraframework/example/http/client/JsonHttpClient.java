package io.koraframework.example.http.client;

import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.json.common.annotation.Json;

@HttpClient("httpClient.default")
public interface JsonHttpClient {

    @Json
    record JsonRequest(String id) {}

    @Json
    record JsonResponse(String name, int value) {}

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    JsonResponse post(@Json JsonRequest body);
}
