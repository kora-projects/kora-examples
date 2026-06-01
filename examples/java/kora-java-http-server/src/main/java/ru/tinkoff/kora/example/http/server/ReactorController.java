package ru.tinkoff.kora.example.http.server;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

/**
 * @see Mono - Method is using Project Reactor reactive response type
 */
@Component
@HttpController
public final class ReactorController {

    @HttpRoute(method = HttpMethod.GET, path = "/reactor/mono")
    public Mono<HttpServerResponse> get() {
        return Mono.fromCallable(() -> HttpServerResponse.of(200, HttpBody.plaintext("Hello world")));
    }
}
