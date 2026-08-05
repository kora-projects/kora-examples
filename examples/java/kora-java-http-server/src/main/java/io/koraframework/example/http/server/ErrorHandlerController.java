package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.InterceptWith;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.annotation.Json;

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
        public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
            try {
                return chain.process(request);
            } catch (Exception e) {
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

                return HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)));
            }
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
