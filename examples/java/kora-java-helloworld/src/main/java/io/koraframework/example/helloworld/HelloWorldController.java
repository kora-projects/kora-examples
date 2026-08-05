package io.koraframework.example.helloworld;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

@Component
@HttpController
public final class HelloWorldController {

    @Json
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
