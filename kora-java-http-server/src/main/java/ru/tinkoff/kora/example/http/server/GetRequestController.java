package ru.tinkoff.kora.example.http.server;

import java.util.Collection;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

/**
 * @see HttpServerRequest - Method receives full request as input parameter
 */
@Component
@HttpController
public final class GetRequestController {

    @HttpRoute(method = HttpMethod.GET, path = "/request")
    public HttpServerResponse get(HttpServerRequest request) {
        final Collection<String> queries = request.queryParams().get("Queries");
        var query = request.queryParams().get("query");
        final String queryValue = (query == null)
                ? null
                : query.stream().findFirst().orElse(null);

        final String header = request.headers().getFirst("header");
        final List<String> headers = request.headers().getAll("Headers");

        final String body = "Path: " + request.path()
                + ", Query: " + queryValue + ", Queries: " + queries
                + ", Header: " + header + ", Headers: " + headers;
        return HttpServerResponse.of(200, HttpBody.plaintext(body));
    }
}
