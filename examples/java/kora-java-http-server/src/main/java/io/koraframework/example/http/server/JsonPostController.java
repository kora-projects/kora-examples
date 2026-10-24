package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

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
