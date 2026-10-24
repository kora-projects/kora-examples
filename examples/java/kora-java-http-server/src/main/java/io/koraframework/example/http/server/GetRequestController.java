package io.koraframework.example.http.server;

import java.util.Collection;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

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
