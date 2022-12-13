package ru.tinkoff.kora.example.crud.controller;

import io.micrometer.core.instrument.config.validate.ValidationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.MessageTO;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.*;
import ru.tinkoff.kora.json.common.JsonWriter;

@Tag(HttpServerModule.class)
@Component
public final class HttpExceptionHandler implements HttpServerInterceptor {

    private final JsonWriter<MessageTO> errorJsonWriter;

    public HttpExceptionHandler(JsonWriter<MessageTO> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        return chain.process(context, request).exceptionally(e -> {
            if (e instanceof HttpServerResponseException ex) {
                return ex;
            }

            var body = HttpBody.json(errorJsonWriter.toByteArrayUnchecked(new MessageTO(e.getMessage())));
            if (e instanceof IllegalArgumentException || e instanceof ValidationException) {
                return HttpServerResponse.of(400, body);
            } else if (e instanceof TimeoutException) {
                return HttpServerResponse.of(408, body);
            } else {
                return HttpServerResponse.of(500, body);
            }
        });
    }
}
