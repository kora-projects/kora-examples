package io.koraframework.example.telemetry;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

@Component
@HttpController
public final class SimpleController {
    private final TraceRepository repository;

    public SimpleController(TraceRepository repository) {
        this.repository = repository;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/text")
    public HttpServerResponse get() {
        var databaseValue = repository.selectOne();
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world: " + databaseValue));
    }
}
