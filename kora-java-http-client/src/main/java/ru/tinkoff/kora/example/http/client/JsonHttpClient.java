package ru.tinkoff.kora.example.http.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpClient(configPath = "httpClient.default")
public interface JsonHttpClient {

    record JsonRequest(String id) {}

    record JsonResponse(String name, int value) {}

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    JsonResponse post(@Json JsonRequest body);
}
