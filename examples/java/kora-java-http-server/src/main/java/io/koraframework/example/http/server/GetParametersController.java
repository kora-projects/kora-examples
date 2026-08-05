package io.koraframework.example.http.server;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.header.HttpHeaders;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

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
