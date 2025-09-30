package ru.tinkoff.kora.example.helloworld;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class HelloWorldController {

    public record HelloWorldResponse(String greeting) {}

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/hello/world/json")
    public HelloWorldResponse helloWorldJson() {
        return new HelloWorldResponse("Hello World");
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/hello/world/json/entity")
    public HttpResponseEntity<HelloWorldResponse> helloWorldJsonEntity() {
        return HttpResponseEntity.of(200, new HelloWorldResponse("Hello World"));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/hello/world")
    public HttpServerResponse helloWorld() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello World"));
    }
}
