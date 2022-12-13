package ru.tinkoff.kora.example.telemetry;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Component
@HttpController
public final class SimpleController {

    @HttpRoute(method = HttpMethod.GET, path = "/text")
    public HttpServerResponse get() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"));
    }
}
