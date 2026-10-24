package io.koraframework.example.http.server;

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
import io.koraframework.validation.common.annotation.Pattern;
import io.koraframework.validation.common.annotation.Size;
import io.koraframework.validation.common.annotation.Validate;

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
