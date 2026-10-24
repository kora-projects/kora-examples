package io.koraframework.guide.gettingstarted;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

@Component
@HttpController
public final class HelloController {

    @HttpRoute(method = HttpMethod.GET, path = "/hello")
    public HttpServerResponse hello() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello, Kora!"));
    }
}
