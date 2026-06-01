package ru.tinkoff.kora.example.http.server;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.InterceptWith;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.common.annotation.Json;

/**
 * @see ErrorHandlerInterceptor - Intercepts all controler methods and handles exceptions
 */
@InterceptWith(ErrorHandlerController.ErrorHandlerInterceptor.class)
@Component
@HttpController
public final class ErrorHandlerController {

    @Json
    public record Error(String id, String code, String message) {}

    @Component
    public static final class ErrorHandlerInterceptor implements HttpServerInterceptor {

        private final JsonWriter<Error> errorJsonWriter;

        public ErrorHandlerInterceptor(JsonWriter<Error> errorJsonWriter) {
            this.errorJsonWriter = errorJsonWriter;
        }

        @Override
        public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
                throws Exception {
            return chain.process(context, request).exceptionally(e -> {
                if (e instanceof HttpServerResponseException ex) {
                    return ex;
                }

                final Error error;
                final int code;
                if (e instanceof IllegalStateException) {
                    error = new Error("1", "BAD_REQUEST", e.getMessage());
                    code = 400;
                } else {
                    error = new Error("1", "INTERNAL_ERROR", e.getMessage());
                    code = 500;
                }

                try {
                    return HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)));
                } catch (IOException ex) {
                    return HttpServerResponse.of(500, HttpBody.plaintext(ex.getMessage()));
                }
            });
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/intercepted/error/{id}")
    public HttpServerResponse get(@Path int id) {
        if (id < 100) {
            throw new IllegalStateException("ID can't be less 100");
        } else {
            return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"));
        }
    }
}
