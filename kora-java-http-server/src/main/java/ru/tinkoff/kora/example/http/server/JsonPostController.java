package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

/**
 * @see Json - Indicates that response should be serialized as JSON
 * @see HttpMethod#POST - Indicates that POST request is expected
 */
@Component
@HttpController
public final class JsonPostController {

    public record Request(String name) {}

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    public HttpServerResponse post(@Json Request request) {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world: " + request.name()));
    }
}
