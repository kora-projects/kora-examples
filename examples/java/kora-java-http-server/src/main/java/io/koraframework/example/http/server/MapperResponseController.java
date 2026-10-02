package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.http.server.common.response.HttpServerResponseMapper;

@Component
@HttpController
public final class MapperResponseController {

    public record HelloWorldResponse(String greeting, String name) {}

    @Component
    public static final class HelloWorldResponseMapper implements HttpServerResponseMapper<HelloWorldResponse> {

        @Override
        public HttpServerResponse apply(HttpServerRequest request, HelloWorldResponse result) {
            return HttpServerResponse.of(200, HttpBody.plaintext(result.greeting() + " - " + result.name()));
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/response/{name}")
    @Mapping(HelloWorldResponseMapper.class)
    public HelloWorldResponse get(@Path String name) {
        return new HelloWorldResponse("Hello World", name);
    }
}
