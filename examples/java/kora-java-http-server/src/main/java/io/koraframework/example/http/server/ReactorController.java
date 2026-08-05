package io.koraframework.example.http.server;

import reactor.core.publisher.Mono;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

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
