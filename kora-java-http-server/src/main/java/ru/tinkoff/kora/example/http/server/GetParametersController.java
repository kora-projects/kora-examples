package ru.tinkoff.kora.example.http.server;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.Header;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.annotation.Query;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.header.HttpHeaders;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

/**
 * @see Path - Treats http path part as method parameter
 * @see Query - Treats http query parameter as method parameter
 * @see Header - Treats http header as method parameter
 */
@Component
@HttpController
public final class GetParametersController {

    @HttpRoute(method = HttpMethod.GET, path = "/parameters/{path}")
    public HttpServerResponse get(@Path String path,
                                  @Nullable @Query String query,
                                  @Nullable @Query("Queries") List<String> queries,
                                  @Nullable @Header String header,
                                  @Nullable @Header("Headers") List<String> headers) {
        final String body = "Path: " + path
                + ", Query: " + query + ", Queries: " + queries
                + ", Header: " + header + ", Headers: " + headers;

        return HttpServerResponse.of(
                200,
                HttpHeaders.of("headerName", "headerValue"),
                HttpBody.plaintext(body));
    }
}
