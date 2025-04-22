package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

/**
 * @see Json - Indicates that response should be serialized as JSON
 * @see HttpMethod#GET - Indicates that GET request is expected
 */
@Component
@HttpController
public final class JsonGetController {

    @Json
    public record HelloWorldResponse(String greeting) {}

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json")
    public HelloWorldResponse get() {
        return new HelloWorldResponse("Hello world");
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json/entity")
    public HttpResponseEntity<HelloWorldResponse> getEntity() {
        return HttpResponseEntity.of(201, new HelloWorldResponse("Hello world"));
    }
}
