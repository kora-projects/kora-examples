package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

/**
 * @see HttpServerResponse - Respond in sync mode with simple HttpResponse
 */
@Component
@HttpController
public final class SyncController {

    @HttpRoute(method = HttpMethod.GET, path = "/sync")
    public HttpServerResponse get() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"));
    }
}
