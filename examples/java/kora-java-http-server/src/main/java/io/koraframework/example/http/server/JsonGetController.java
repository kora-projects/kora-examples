package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

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
