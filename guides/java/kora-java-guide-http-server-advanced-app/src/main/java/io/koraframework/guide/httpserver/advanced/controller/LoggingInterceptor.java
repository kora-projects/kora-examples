package io.koraframework.guide.httpserver.advanced.controller;

import java.util.concurrent.CompletionStage;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.Context;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;

@Component
public final class LoggingInterceptor implements HttpServerInterceptor {

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        long started = System.nanoTime();
        return chain.process(context, request).whenComplete((response, throwable) -> {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            int statusCode = response != null ? response.code() : 500;
            System.out.printf("Request: %s %s -> %d (%d ms)%n", request.method(), request.path(), statusCode, durationMs);
        });
    }
}
