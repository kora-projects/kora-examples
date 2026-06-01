package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.ErrorResponseTO;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.validation.common.ViolationException;

@Component
public final class DataApiExceptionHandler implements HttpServerInterceptor {

    private final JsonWriter<ErrorResponseTO> errorJsonWriter;

    public DataApiExceptionHandler(JsonWriter<ErrorResponseTO> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        return chain.process(context, request).exceptionally(throwable -> {
            var cause = unwrap(throwable);
            if (cause instanceof ViolationException violationException) {
                throw new CompletionException(violationException);
            }
            if (cause instanceof HttpServerResponseException responseException) {
                return jsonResponse(responseException.code(), responseException.getMessage());
            }
            if (cause instanceof IllegalArgumentException) {
                return jsonResponse(400, "Invalid request parameters");
            }
            if (cause instanceof SecurityException) {
                return jsonResponse(403, cause.getMessage() != null ? cause.getMessage() : "Access denied");
            }
            return jsonResponse(500, "An unexpected error occurred");
        });
    }

    private HttpServerResponse jsonResponse(int statusCode, String message) {
        return HttpServerResponse.of(statusCode, HttpBody.json(this.errorJsonWriter.toByteArrayUnchecked(new ErrorResponseTO(message, null))));
    }

    private static Throwable unwrap(Throwable throwable) {
        var current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}

