package ru.tinkoff.kora.guide.httpserver.advanced.controller;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.httpserver.advanced.dto.ErrorResponse;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerModule;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.json.common.JsonWriter;

@Tag(HttpServerModule.class)
@Component
public final class ExceptionHandler implements HttpServerInterceptor {

    private final JsonWriter<ErrorResponse> errorJsonWriter;

    public ExceptionHandler(JsonWriter<ErrorResponse> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        return chain.process(context, request).exceptionally(throwable -> {
            Throwable cause = unwrap(throwable);
            if (cause instanceof RestrictedFormNameException restrictedFormNameException) {
                return jsonResponse(400, restrictedFormNameException.getMessage());
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
        return HttpServerResponse.of(statusCode, HttpBody.json(this.errorJsonWriter.toByteArrayUnchecked(new ErrorResponse(message))));
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}


