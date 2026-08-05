package io.koraframework.guide.s3.http;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.Context;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.s3.dto.ErrorResponse;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.HttpServer;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.json.common.JsonWriter;

@Tag(HttpServer.class)
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

