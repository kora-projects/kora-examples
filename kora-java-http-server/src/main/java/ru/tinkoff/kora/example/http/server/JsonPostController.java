package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

/**
 * @see Json - Indicates that response should be serialized as JSON
 * @see HttpMethod#POST - Indicates that POST request is expected
 */
@Component
@HttpController
public final class JsonPostController {

    @Json
    public record JsonRequest(String id) {}

    @Json
    public record JsonResponse(String name, int value) {}

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    public JsonResponse post(@Json JsonRequest request) {
        return new JsonResponse("Ivan", 100);
    }
}
