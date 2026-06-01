package ru.tinkoff.kora.example.http.server;

import java.util.stream.Collectors;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

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
