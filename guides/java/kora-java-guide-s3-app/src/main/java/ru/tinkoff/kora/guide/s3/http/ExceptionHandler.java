package ru.tinkoff.kora.guide.s3.http;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.s3.dto.ErrorResponse;
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
            if (cause instanceof HttpServerResponseException responseException) {
                return HttpServerResponse.of(responseException.code(),
                        HttpBody.json(errorJsonWriter.toByteArrayUnchecked(
                                ErrorResponse.of("HTTP_" + responseException.code(), responseException.getMessage()))));
            }
            if (cause instanceof IllegalArgumentException) {
                return HttpServerResponse.of(400,
                        HttpBody.json(errorJsonWriter.toByteArrayUnchecked(
                                ErrorResponse.of("BAD_REQUEST", "Invalid request parameters"))));
            }
            if (cause instanceof SecurityException) {
                return HttpServerResponse.of(403,
                        HttpBody.json(errorJsonWriter.toByteArrayUnchecked(
                                ErrorResponse.of("FORBIDDEN", "Access denied"))));
            }
            return HttpServerResponse.of(500,
                    HttpBody.json(errorJsonWriter.toByteArrayUnchecked(
                            ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"))));
        });
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}

