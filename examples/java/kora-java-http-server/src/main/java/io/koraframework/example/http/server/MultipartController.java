package io.koraframework.example.http.server;

import java.util.stream.Collectors;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.form.FormMultipart;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

/**
 * @see HttpMethod#POST - Indicates that POST request is expected
 * @see FormMultipart - Multipart request body
 */
@Component
@HttpController
public final class MultipartController {

    @HttpRoute(method = HttpMethod.POST, path = "/multipart")
    public HttpServerResponse post(FormMultipart multipart) {
        final String partAsString = multipart.parts().stream()
                .map(FormMultipart.FormPart::name)
                .sorted()
                .collect(Collectors.joining(","));

        return HttpServerResponse.of(200, HttpBody.plaintext(partAsString));
    }
}
