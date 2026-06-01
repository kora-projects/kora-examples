package ru.tinkoff.kora.guide.gettingstarted;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Component
@HttpController
public final class HelloController {

    @HttpRoute(method = HttpMethod.GET, path = "/hello")
    public HttpServerResponse hello() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello, Kora!"));
    }
}
