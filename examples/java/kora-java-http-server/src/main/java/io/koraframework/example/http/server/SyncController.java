package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

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
