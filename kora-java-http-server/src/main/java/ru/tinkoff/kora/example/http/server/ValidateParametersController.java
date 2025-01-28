package ru.tinkoff.kora.example.http.server;

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
import ru.tinkoff.kora.validation.common.annotation.Pattern;
import ru.tinkoff.kora.validation.common.annotation.Size;
import ru.tinkoff.kora.validation.common.annotation.Validate;

/**
 * @see Path - Treats http path part as method parameter
 * @see Query - Treats http query parameter as method parameter
 * @see Header - Treats http header as method parameter
 */
@Component
@HttpController
public class ValidateParametersController {

    @Validate
    @HttpRoute(method = HttpMethod.GET, path = "/validate/{path}")
    public HttpServerResponse get(@Path String path,
                                  @Pattern("q.+") @Query String query,
                                  @Size(min = 2, max = Integer.MAX_VALUE) @Query("Queries") List<String> queries,
                                  @Pattern("h.+") @Header String header,
                                  @Size(min = 2, max = Integer.MAX_VALUE) @Header("Headers") List<String> headers) {
        final String body = "Path: " + path
                + ", Query: " + query + ", Queries: " + queries
                + ", Header: " + header + ", Headers: " + headers;

        return HttpServerResponse.of(
                200,
                HttpHeaders.of("headerName", "headerValue"),
                HttpBody.plaintext(body));
    }
}
