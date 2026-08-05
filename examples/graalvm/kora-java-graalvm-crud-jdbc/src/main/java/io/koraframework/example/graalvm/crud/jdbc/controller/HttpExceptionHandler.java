package io.koraframework.example.graalvm.crud.jdbc.controller;

import io.micrometer.core.instrument.config.validate.ValidationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.Context;
import io.koraframework.common.annotation.Tag;
import io.koraframework.example.graalvm.crud.openapi.server.model.MessageTO;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.*;
import io.koraframework.json.common.JsonWriter;

@Tag(HttpServerModule.class)
@Component
public final class HttpExceptionHandler implements HttpServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpExceptionHandler.class);

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
                logger.error("Request '{} {}' failed", request.method(), request.path(), e);
                return HttpServerResponse.of(500, body);
            }
        });
    }
}
