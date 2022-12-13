package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.http.server.common.handler.HttpServerResponseMapper;

@Component
@HttpController
public final class MapperResponseController {

    public record HelloWorldResponse(String greeting, String name) {}

    public static final class HelloWorldResponseMapper implements HttpServerResponseMapper<HelloWorldResponse> {

        @Override
        public HttpServerResponse apply(Context ctx, HttpServerRequest request, HelloWorldResponse result) {
            return HttpServerResponse.of(200, HttpBody.plaintext(result.greeting() + " - " + result.name()));
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/response/{name}")
    @Mapping(HelloWorldResponseMapper.class)
    public HelloWorldResponse get(@Path String name) {
        return new HelloWorldResponse("Hello World", name);
    }
}
